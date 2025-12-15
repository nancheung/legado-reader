package com.nancheung.plugins.jetbrains.legadoreader.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.manager.BodyInLineDataManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 阅读全局门面
 * 协调 API 调用和事件发布，解耦 Action/UI/API 层
 * <p>
 * 架构说明：
 * - nextChapter/previousChapter：使用事件驱动架构（发布 ReadingEvent）
 * - nextPage/previousPage：使用广播模式（调用 execute 通知各 Reader）
 * - 翻页到章节边界时，自动切换到事件驱动的章节切换逻辑
 *
 * @author NanCheung
 */
@Slf4j
@Service
public final class ReaderGlobalFacade implements IReader {

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static ReaderGlobalFacade getInstance() {
        return ApplicationManager.getApplication().getService(ReaderGlobalFacade.class);
    }

    /**
     * 获取事件发布者
     *
     * @return 事件发布者
     */
    private ReadingEventListener getPublisher() {
        return ApplicationManager.getApplication()
                .getMessageBus()
                .syncPublisher(ReadingEventListener.TOPIC);
    }
    
    @Override
    public void previousPage() {
        // 获取行内阅读服务的分页管理器
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        BodyInLineDataManager.LineData currentLine = dataManager.getCurrentLine();

        if (currentLine == null) {
            log.debug("当前页为空，无法翻页");
            return;
        }

        // 判断是否在第一页
        if (currentLine.getLineIndex() > 0) {
            // 不是第一页，广播页内翻页操作
            execute(IReader::previousPage);
        } else {
            // 第一页，触发章节切换
            log.debug("已经是第一页，切换到上一章");
            previousChapter();
        }
    }

    @Override
    public void nextPage() {
        // 获取行内阅读服务的分页管理器
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        BodyInLineDataManager.LineData currentLine = dataManager.getCurrentLine();

        if (currentLine == null) {
            log.debug("当前页为空，无法翻页");
            return;
        }

        List<BodyInLineDataManager.LineData> lineContentList = dataManager.getLineContentList();
        int currentLineIndex = currentLine.getLineIndex();

        // 判断是否在最后一页
        if (currentLineIndex < lineContentList.size() - 1) {
            // 不是最后一页，广播页内翻页操作
            execute(IReader::nextPage);
        } else {
            // 最后一页，触发章节切换
            log.debug("已经是最后一页，切换到下一章");
            nextChapter();
        }
    }

    /**
     * 上一章
     * 使用事件驱动架构，不再通过 IReader 接口调用
     */
    public void previousChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        ReadingSession session = sessionManager.getSession();

        if (session == null) {
            log.warn("没有当前阅读会话，无法切换章节");
            return;
        }

        // 1. 边界检测：第一章
        if (sessionManager.getCurrentChapterIndex() < 1) {
            log.debug("已经是第一章");
            return;
        }

        // 2. 获取书籍信息和目标章节索引
        BookDTO book = session.book();
        int previousIndex = sessionManager.getCurrentChapterIndex() - 1;

        log.info("切换到上一章: {} -> {}", sessionManager.getCurrentChapterIndex(), previousIndex);

        // 3. 立即发布 LOADING_STARTED 事件（使用临时章节对象）
        BookChapterDTO tempChapter = new BookChapterDTO();
        tempChapter.setIndex(previousIndex);
        ReadingEvent startEvent = ReadingEvent.loadingStarted(book, tempChapter, ReadingEvent.Direction.PREVIOUS);
        getPublisher().onReadingEvent(startEvent);

        // 4. 更新章节索引（提前更新）
        sessionManager.previousChapter();

        // 5. 异步加载数据
        CompletableFuture.runAsync(() -> {
            try {
                // 获取章节列表（从会话中）
                List<BookChapterDTO> chapters = sessionManager.getChapters();
                BookChapterDTO chapter = chapters.get(previousIndex);

                // 获取章节内容
                String content = ApiUtil.getBookContent(book.getBookUrl(), previousIndex);

                // 更新会话内容
                sessionManager.setContent(content);

                // 发布 LOADING_SUCCESS 事件
                ReadingEvent successEvent = ReadingEvent.loadingSuccess(book, chapter, content, 0, ReadingEvent.Direction.PREVIOUS);
                getPublisher().onReadingEvent(successEvent);

                log.info("切换到上一章成功：{}", chapter.getTitle());

                // 异步同步进度（不等待响应）
                CompletableFuture.runAsync(() -> {
                    try {
                        ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), previousIndex, chapter.getTitle(), 0);
                    } catch (Exception e) {
                        if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                            log.error("同步阅读进度失败", e);
                        }
                    }
                });

            } catch (Exception e) {
                // 回滚索引
                sessionManager.nextChapter();

                // 发布 LOADING_FAILED 事件
                BookChapterDTO failedChapter = new BookChapterDTO();
                failedChapter.setIndex(previousIndex);
                ReadingEvent failedEvent = ReadingEvent.loadingFailed(book, failedChapter, e, ReadingEvent.Direction.PREVIOUS);
                getPublisher().onReadingEvent(failedEvent);

                if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                    log.error("章节加载失败", e);
                }
            }
        });
    }

    /**
     * 下一章
     * 使用事件驱动架构，不再通过 IReader 接口调用
     */
    public void nextChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        ReadingSession session = sessionManager.getSession();

        if (session == null) {
            log.warn("没有当前阅读会话，无法切换章节");
            return;
        }

        // 1. 边界检测：最后一章
        int currentIndex = sessionManager.getCurrentChapterIndex();
        int totalChapters = session.chapters().size();

        if (currentIndex >= totalChapters - 1) {
            log.debug("已经是最后一章");
            return;
        }

        // 2. 获取书籍信息和目标章节索引
        BookDTO book = session.book();
        int nextIndex = currentIndex + 1;

        log.info("切换到下一章: {} -> {}", currentIndex, nextIndex);

        // 3. 立即发布 LOADING_STARTED 事件（使用临时章节对象）
        BookChapterDTO tempChapter = new BookChapterDTO();
        tempChapter.setIndex(nextIndex);
        ReadingEvent startEvent = ReadingEvent.loadingStarted(book, tempChapter, ReadingEvent.Direction.NEXT);
        getPublisher().onReadingEvent(startEvent);

        // 4. 更新章节索引（提前更新）
        sessionManager.nextChapter();

        // 5. 异步加载数据
        CompletableFuture.runAsync(() -> {
            try {
                // 获取章节列表（从会话中）
                List<BookChapterDTO> chapters = sessionManager.getChapters();
                BookChapterDTO chapter = chapters.get(nextIndex);

                // 获取章节内容
                String content = ApiUtil.getBookContent(book.getBookUrl(), nextIndex);

                // 更新会话内容
                sessionManager.setContent(content);

                // 发布 LOADING_SUCCESS 事件
                ReadingEvent successEvent = ReadingEvent.loadingSuccess(book, chapter, content, 0, ReadingEvent.Direction.NEXT);
                getPublisher().onReadingEvent(successEvent);

                log.info("切换到下一章成功：{}", chapter.getTitle());

                // 异步同步进度（不等待响应）
                CompletableFuture.runAsync(() -> {
                    try {
                        ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), nextIndex, chapter.getTitle(), 0);
                    } catch (Exception e) {
                        if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                            log.error("同步阅读进度失败", e);
                        }
                    }
                });

            } catch (Exception e) {
                // 回滚索引
                sessionManager.previousChapter();

                // 发布 LOADING_FAILED 事件
                BookChapterDTO failedChapter = new BookChapterDTO();
                failedChapter.setIndex(nextIndex);
                ReadingEvent failedEvent = ReadingEvent.loadingFailed(book, failedChapter, e, ReadingEvent.Direction.NEXT);
                getPublisher().onReadingEvent(failedEvent);

                if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                    log.error("章节加载失败", e);
                }
            }
        });
    }

    /**
     * 加载章节（统一入口）
     * 适用于：首次打开书籍、从目录跳转到指定章节
     *
     * @param book         书籍信息
     * @param chapterIndex 目标章节索引
     */
    public void loadChapter(BookDTO book, int chapterIndex) {
        log.info("加载章节: book={}, chapterIndex={}", book.getName(), chapterIndex);

        // 1. 创建临时章节对象，立即发布 LOADING_STARTED 事件
        BookChapterDTO tempChapter = new BookChapterDTO();
        tempChapter.setIndex(chapterIndex);
        ReadingEvent startEvent = ReadingEvent.loadingStarted(book, tempChapter, ReadingEvent.Direction.JUMP);
        getPublisher().onReadingEvent(startEvent);

        // 2. 异步获取章节列表和内容
        CompletableFuture.runAsync(() -> {
            try {
                // 获取章节列表
                List<BookChapterDTO> chapters = ApiUtil.getChapterList(book.getBookUrl());

                // 边界检查
                if (chapterIndex < 0 || chapterIndex >= chapters.size()) {
                    throw new IllegalArgumentException("章节索引越界: " + chapterIndex);
                }

                BookChapterDTO chapter = chapters.get(chapterIndex);

                // 获取章节内容
                String content = ApiUtil.getBookContent(book.getBookUrl(), chapterIndex);

                // 创建并设置会话
                ReadingSession session = new ReadingSession(book, chapters, chapterIndex, content);
                ReadingSessionManager.getInstance().setSession(session);

                // 发布 LOADING_SUCCESS 事件
                int position = (chapterIndex == book.getDurChapterIndex()) ? book.getDurChapterPos() : 0;
                ReadingEvent successEvent = ReadingEvent.loadingSuccess(book, chapter, content, position, ReadingEvent.Direction.JUMP);
                getPublisher().onReadingEvent(successEvent);

                log.info("章节加载成功: {}", chapter.getTitle());

                // 异步同步进度
                CompletableFuture.runAsync(() -> {
                    try {
                        ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), chapterIndex, chapter.getTitle(), position);
                    } catch (Exception e) {
                        if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                            log.error("同步阅读进度失败", e);
                        }
                    }
                });

            } catch (Exception e) {
                // 发布 LOADING_FAILED 事件
                BookChapterDTO failedChapter = new BookChapterDTO();
                failedChapter.setIndex(chapterIndex);
                ReadingEvent failedEvent = ReadingEvent.loadingFailed(book, failedChapter, e, ReadingEvent.Direction.JUMP);
                getPublisher().onReadingEvent(failedEvent);

                if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                    log.error("章节加载失败", e);
                }
            }
        });
    }

    @Override
    public void splitChapter(String chapterContent, int pageSize) {
        execute(reader -> reader.splitChapter(chapterContent, pageSize));
    }
    
    private void execute(Consumer<IReader> consumer) {
        for (ReaderFactory readerFactory : ReaderFactory.READER_FACTORYS) {
            CompletableFuture.runAsync(() -> consumer.accept(readerFactory.getReader()));
        }
    }
}
