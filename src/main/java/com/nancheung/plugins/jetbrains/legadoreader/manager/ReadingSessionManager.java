package com.nancheung.plugins.jetbrains.legadoreader.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 阅读会话管理器（Application Service）
 * 管理当前阅读会话（内存中，不持久化）
 * 使用 AtomicReference 保证线程安全
 *
 * @author NanCheung
 */
@Service
public final class ReadingSessionManager {

    private final AtomicReference<ReadingSession> currentSession = new AtomicReference<>();

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static ReadingSessionManager getInstance() {
        return ApplicationManager.getApplication().getService(ReadingSessionManager.class);
    }

    /**
     * 设置当前会话
     *
     * @param session 会话对象
     */
    public void setSession(ReadingSession session) {
        currentSession.set(session);
    }

    /**
     * 获取当前会话
     *
     * @return 当前会话，可能为 null
     */
    public ReadingSession getSession() {
        return currentSession.get();
    }

    /**
     * 切换到下一章
     */
    public void nextChapter() {
        currentSession.updateAndGet(session ->
                session != null ? session.nextChapter() : null
        );
    }

    /**
     * 切换到上一章
     */
    public void previousChapter() {
        currentSession.updateAndGet(session ->
                session != null ? session.previousChapter() : null
        );
    }

    /**
     * 更新章节索引
     *
     * @param index 新的章节索引
     */
    public void setChapterIndex(int index) {
        currentSession.updateAndGet(session ->
                session != null ? session.withChapterIndex(index) : null
        );
    }

    /**
     * 更新章节内容
     *
     * @param content 章节内容
     */
    public void setContent(String content) {
        currentSession.updateAndGet(session ->
                session != null ? session.withContent(content) : null
        );
    }

    /**
     * 获取当前章节索引
     *
     * @return 章节索引
     */
    public int getCurrentChapterIndex() {
        ReadingSession session = currentSession.get();
        return session != null ? session.currentChapterIndex() : 0;
    }

    /**
     * 获取当前书籍
     *
     * @return 当前书籍，可能为 null
     */
    public BookDTO getCurrentBook() {
        ReadingSession session = currentSession.get();
        return session != null ? session.book() : null;
    }

    /**
     * 获取章节列表
     *
     * @return 章节列表，可能为 null
     */
    public List<BookChapterDTO> getChapters() {
        ReadingSession session = currentSession.get();
        return session != null ? session.chapters() : null;
    }

    /**
     * 获取当前章节
     *
     * @return 当前章节，可能为 null
     */
    public BookChapterDTO getCurrentChapter() {
        ReadingSession session = currentSession.get();
        return session != null ? session.getCurrentChapter() : null;
    }

    /**
     * 获取当前内容
     *
     * @return 当前内容，可能为 null
     */
    public String getCurrentContent() {
        ReadingSession session = currentSession.get();
        return session != null ? session.currentContent() : null;
    }

    /**
     * 清空会话
     */
    public void clear() {
        currentSession.set(null);
    }
}
