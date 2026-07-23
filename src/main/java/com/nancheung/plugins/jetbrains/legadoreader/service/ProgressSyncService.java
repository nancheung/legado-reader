package com.nancheung.plugins.jetbrains.legadoreader.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 阅读进度同步服务（Application Service）
 * 翻页时防抖同步章节内阅读位置到服务器：连续翻页只在停止后同步一次，避免频繁请求
 *
 * @author NanCheung
 */
@Slf4j
@Service
public final class ProgressSyncService implements Disposable {

    /**
     * 防抖延迟：停止翻页该时长后才执行同步
     */
    private static final long DEBOUNCE_DELAY_MS = 2000;

    /**
     * 单线程调度器（守护线程，不阻塞 IDE 退出）
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "LegadoProgressSync");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 待执行的同步任务（防抖：新任务会取消未执行的旧任务）
     */
    private ScheduledFuture<?> pendingFuture;
    private Runnable pendingTask;

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static ProgressSyncService getInstance() {
        return ApplicationManager.getApplication().getService(ProgressSyncService.class);
    }

    /**
     * 防抖同步当前会话在章节内的阅读位置
     * 供翻页处理器、正文滚动监听调用，同步数据在调用时快照，延迟执行时不依赖会话是否存在
     *
     * @param position 章节内字符偏移（durChapterPos）
     */
    public void scheduleSyncCurrentPosition(int position) {
        ReadingSession session = ReadingSessionManager.getInstance().getSession();
        if (session == null) {
            return;
        }

        BookDTO book = session.book();
        BookChapterDTO chapter = session.getCurrentChapter();
        int chapterIndex = session.currentChapterIndex();
        String chapterTitle = chapter != null ? chapter.getTitle() : "";

        scheduleSync(book, chapterIndex, chapterTitle, position);
    }

    /**
     * 防抖同步阅读进度
     *
     * @param book         书籍信息
     * @param chapterIndex 章节索引
     * @param chapterTitle 章节标题
     * @param position     章节内字符偏移（durChapterPos）
     */
    public synchronized void scheduleSync(BookDTO book, int chapterIndex, String chapterTitle, int position) {
        if (book == null) {
            return;
        }

        // 取消未执行的旧任务
        if (pendingFuture != null) {
            pendingFuture.cancel(false);
        }

        // 快照同步数据，延迟执行时不再读取会话状态
        pendingTask = () -> doSync(book, chapterIndex, chapterTitle, position);
        pendingFuture = scheduler.schedule(this::runPending, DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 立即执行待同步的进度（如结束会话前调用，保证最后位置不丢失）
     */
    public synchronized void flush() {
        if (pendingFuture != null && pendingFuture.cancel(false)) {
            Runnable task = pendingTask;
            pendingTask = null;
            pendingFuture = null;
            if (task != null) {
                scheduler.execute(task);
            }
        }
    }

    /**
     * 执行并清空待同步任务
     */
    private void runPending() {
        Runnable task;
        synchronized (this) {
            task = pendingTask;
            pendingTask = null;
            pendingFuture = null;
        }
        if (task != null) {
            task.run();
        }
    }

    /**
     * 执行同步（在调度器线程中运行）
     */
    private void doSync(BookDTO book, int chapterIndex, String chapterTitle, int position) {
        try {
            ApiUtil.saveBookProgress(book.getBookUrl(), book.getAuthor(), book.getName(),
                    chapterIndex, chapterTitle, position);
            log.info("同步页内阅读进度成功：{} - {} - 位置 {}", book.getName(), chapterTitle, position);
        } catch (Exception e) {
            // 忽略同步失败，不影响阅读体验
            log.warn("同步页内阅读进度失败", e);
        }
    }

    @Override
    public void dispose() {
        // 关闭前执行待同步任务，尽量保住最后进度
        runPending();
        scheduler.shutdownNow();
    }
}
