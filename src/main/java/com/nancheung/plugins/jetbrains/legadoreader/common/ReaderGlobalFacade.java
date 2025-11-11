package com.nancheung.plugins.jetbrains.legadoreader.common;

import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.editorline.EditorLineReaderService;
import com.nancheung.plugins.jetbrains.legadoreader.manager.BodyInLineDataManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
public class ReaderGlobalFacade implements IReader {
    
    private static final ReaderGlobalFacade INSTANCE = new ReaderGlobalFacade();
    
    private ReaderGlobalFacade() {
    }
    
    public static ReaderGlobalFacade getInstance() {
        return INSTANCE;
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
    
    @Override
    public void previousChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();

        // 1. 边界检测：第一章
        if (sessionManager.getCurrentChapterIndex() < 1) {
            log.debug("已经是第一章");
            return;
        }

        // 2. 更新章节索引
        sessionManager.previousChapter();

        // 3. 获取章节信息
        BookDTO book = sessionManager.getCurrentBook();
        BookChapterDTO chapter = sessionManager.getCurrentChapter();
        int chapterIndex = sessionManager.getCurrentChapterIndex();

        if (book == null || chapter == null) {
            log.error("获取章节信息失败");
            sessionManager.nextChapter(); // 回滚
            return;
        }

        // 4. 异步获取章节内容
        CompletableFuture.supplyAsync(() ->
                ApiUtil.getBookContent(book.getBookUrl(), chapterIndex)
        )
        .thenAccept(content -> {
            // 5. 更新会话内容
            sessionManager.setContent(content);

            log.info("切换到上一章：{}", chapter.getTitle());

            // 6. 广播到各 Reader 进行 UI 更新
            execute(IReader::previousChapter);
        })
        .thenRunAsync(() -> {
            // 7. 在 UI 更新后，异步同步阅读进度
            ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), chapterIndex, chapter.getTitle(), 0);
        })
        .exceptionally(throwable -> {
            log.error("获取章节内容失败", throwable);
            // 回滚索引
            sessionManager.nextChapter();
            return null;
        });
    }
    
    @Override
    public void nextChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();

        // 1. 边界检测：最后一章
        List<BookChapterDTO> chapters = sessionManager.getChapters();
        if (chapters == null || sessionManager.getCurrentChapterIndex() >= chapters.size() - 1) {
            log.debug("已经是最后一章");
            return;
        }

        // 2. 更新章节索引
        sessionManager.nextChapter();

        // 3. 获取章节信息
        BookDTO book = sessionManager.getCurrentBook();
        BookChapterDTO chapter = sessionManager.getCurrentChapter();
        int chapterIndex = sessionManager.getCurrentChapterIndex();

        if (book == null || chapter == null) {
            log.error("获取章节信息失败");
            sessionManager.previousChapter(); // 回滚
            return;
        }

        // 4. 异步获取章节内容
        CompletableFuture.supplyAsync(() ->
                ApiUtil.getBookContent(book.getBookUrl(), chapterIndex)
        )
        .thenAccept(content -> {
            // 5. 更新会话内容
            sessionManager.setContent(content);

            log.info("切换到下一章：{}", chapter.getTitle());

            // 6. 广播到各 Reader 进行 UI 更新
            execute(IReader::nextChapter);
        })
        .thenRunAsync(() -> {
            // 7. 在 UI 更新后，异步同步阅读进度
            ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), chapterIndex, chapter.getTitle(), 0);
        })
        .exceptionally(throwable -> {
            log.error("获取章节内容失败", throwable);
            // 回滚索引
            sessionManager.previousChapter();
            return null;
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
