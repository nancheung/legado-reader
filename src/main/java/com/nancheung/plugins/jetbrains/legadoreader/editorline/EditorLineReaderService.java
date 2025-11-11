package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;
import com.nancheung.plugins.jetbrains.legadoreader.manager.BodyInLineDataManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 编辑器行内阅读服务
 * 实现 IReader 接口，提供翻页等阅读操作
 */
@Slf4j
public class EditorLineReaderService implements IReader {

    /**
     * 上一页
     * 如果已经是第一页，则自动切换到上一章的最后一页
     */
    @Override
    public void previousPage() {
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        BodyInLineDataManager.LineData currentLine = dataManager.getCurrentLine();

        if (currentLine == null) {
            log.debug("当前页为空");
            return;
        }

        int currentLineIndex = currentLine.getLineIndex();

        if (currentLineIndex > 0) {
            // 不是第一页，正常翻页
            List<BodyInLineDataManager.LineData> lineContentList = dataManager.getLineContentList();
            dataManager.setCurrentLine(lineContentList.get(currentLineIndex - 1));
            refreshEditor();
            log.debug("翻到上一页: {}", currentLineIndex);
        } else {
            // 第一页，自动切换到上一章
            log.debug("已经是第一页，尝试切换到上一章");
            previousChapter();
        }
    }

    /**
     * 下一页
     * 如果已经是最后一页，则自动切换到下一章的第一页
     */
    @Override
    public void nextPage() {
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        BodyInLineDataManager.LineData currentLine = dataManager.getCurrentLine();

        if (currentLine == null) {
            log.debug("当前页为空");
            return;
        }

        List<BodyInLineDataManager.LineData> lineContentList = dataManager.getLineContentList();
        int currentLineIndex = currentLine.getLineIndex();

        if (currentLineIndex < lineContentList.size() - 1) {
            // 不是最后一页，正常翻页
            dataManager.setCurrentLine(lineContentList.get(currentLineIndex + 1));
            refreshEditor();
            log.debug("翻到下一页: {}", currentLineIndex + 2);
        } else {
            // 最后一页，自动切换到下一章
            log.debug("已经是最后一页，尝试切换到下一章");
            nextChapter();
        }
    }

    /**
     * 上一章
     * 自动定位到上一章的最后一页
     */
    @Override
    public void previousChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();

        // 边界检测：第一章
        if (sessionManager.getCurrentChapterIndex() < 1) {
            log.debug("已经是第一章");
            return;
        }

        // 更新章节索引
        sessionManager.previousChapter();

        // 获取章节信息
        BookDTO book = sessionManager.getCurrentBook();
        BookChapterDTO chapter = sessionManager.getCurrentChapter();

        if (book == null || chapter == null) {
            log.error("获取章节信息失败");
            sessionManager.nextChapter(); // 回滚
            return;
        }

        // 异步获取新章节内容
        CompletableFuture.supplyAsync(() ->
                ApiUtil.getBookContent(book.getBookUrl(), sessionManager.getCurrentChapterIndex())
        )
        .thenAccept(content -> {
            // 更新内容
            sessionManager.setContent(content);

            // 重新分页
            dataManager.initCurrent(content);

            // 定位到最后一页
            List<BodyInLineDataManager.LineData> pages = dataManager.getLineContentList();
            if (!pages.isEmpty()) {
                dataManager.setCurrentLine(pages.get(pages.size() - 1));
            }

            // 刷新 UI
            refreshEditor();

            log.info("切换到上一章：{}", chapter.getTitle());
        })
        .exceptionally(throwable -> {
            log.error("获取章节内容失败", throwable);
            // 回滚索引
            sessionManager.nextChapter();
            return null;
        });
    }

    /**
     * 下一章
     * 自动定位到下一章的第一页
     */
    @Override
    public void nextChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();

        // 边界检测：最后一章
        List<BookChapterDTO> chapters = sessionManager.getChapters();
        if (chapters == null || sessionManager.getCurrentChapterIndex() >= chapters.size() - 1) {
            log.debug("已经是最后一章");
            return;
        }

        // 更新章节索引
        sessionManager.nextChapter();

        // 获取章节信息
        BookDTO book = sessionManager.getCurrentBook();
        BookChapterDTO chapter = sessionManager.getCurrentChapter();

        if (book == null || chapter == null) {
            log.error("获取章节信息失败");
            sessionManager.previousChapter(); // 回滚
            return;
        }

        // 异步获取新章节内容
        CompletableFuture.supplyAsync(() ->
                ApiUtil.getBookContent(book.getBookUrl(), sessionManager.getCurrentChapterIndex())
        )
        .thenAccept(content -> {
            // 更新内容
            sessionManager.setContent(content);

            // 重新分页（自动定位到第一页）
            dataManager.initCurrent(content);

            // 刷新 UI
            refreshEditor();

            log.info("切换到下一章：{}", chapter.getTitle());
        })
        .exceptionally(throwable -> {
            log.error("获取章节内容失败", throwable);
            // 回滚索引
            sessionManager.previousChapter();
            return null;
        });
    }

    /**
     * 将整章内容分页
     *
     * @param chapterContent 章节内容（此参数被忽略，使用当前会话内容）
     * @param pageSize       每页大小
     */
    @Override
    public void splitChapter(String chapterContent, int pageSize) {
        log.debug("开始分页，每页大小: " + pageSize);

        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        dataManager.setLineMaxLength(pageSize);
        dataManager.initCurrent();

        log.info("分页完成，共 " + dataManager.getLineContentList().size() + " 页");

        // 触发 UI 刷新
        refreshEditor();


    }

    /**
     * 刷新编辑器，触发行内内容重绘
     * 在 EDT 线程中执行，确保线程安全
     */
    private void refreshEditor() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // 获取当前打开的项目
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

            for (Project project : openProjects) {
                if (project != null && !project.isDisposed()) {
                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    if (editor != null && !editor.isDisposed()) {
                        // 触发编辑器内容组件重绘
                        editor.getContentComponent().repaint();
                        log.debug("触发编辑器重绘");
                    }
                }
            }
        });
    }
}
