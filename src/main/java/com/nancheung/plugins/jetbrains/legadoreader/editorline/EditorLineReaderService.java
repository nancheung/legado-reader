package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;
import com.nancheung.plugins.jetbrains.legadoreader.manager.BodyInLineDataManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 编辑器行内阅读服务
 * 实现 IReader 接口，提供翻页等阅读操作
 */
@Slf4j
public class EditorLineReaderService implements IReader {

    /**
     * 上一页（页内翻页，不跨章节）
     * 跨章节逻辑由 ReaderGlobalFacade 处理
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
        }
        // 如果是第一页，由 ReaderGlobalFacade 调用 previousChapter()
    }

    /**
     * 下一页（页内翻页，不跨章节）
     * 跨章节逻辑由 ReaderGlobalFacade 处理
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
        }
        // 如果是最后一页，由 ReaderGlobalFacade 调用 nextChapter()
    }

    /**
     * 上一章（UI 更新逻辑，数据操作由 ReaderGlobalFacade 完成）
     * 自动定位到上一章的最后一页
     */
    @Override
    public void previousChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();

        // ReaderGlobalFacade 已完成数据操作，这里只处理 UI 差异
        String content = sessionManager.getCurrentContent();
        BookChapterDTO chapter = sessionManager.getCurrentChapter();

        // 重新分页
        dataManager.initCurrent(content);

        // 定位到最后一页
        List<BodyInLineDataManager.LineData> pages = dataManager.getLineContentList();
        if (!pages.isEmpty()) {
            dataManager.setCurrentLine(pages.get(pages.size() - 1));
        }

        // 刷新 UI
        refreshEditor();

        log.info("UI 更新完成 - 上一章：{}", chapter != null ? chapter.getTitle() : "未知");
    }

    /**
     * 下一章（UI 更新逻辑，数据操作由 ReaderGlobalFacade 完成）
     * 自动定位到下一章的第一页
     */
    @Override
    public void nextChapter() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();

        // ReaderGlobalFacade 已完成数据操作，这里只处理 UI 差异
        String content = sessionManager.getCurrentContent();
        BookChapterDTO chapter = sessionManager.getCurrentChapter();

        // 重新分页（自动定位到第一页）
        dataManager.initCurrent(content);

        // 刷新 UI
        refreshEditor();

        log.info("UI 更新完成 - 下一章：{}", chapter != null ? chapter.getTitle() : "未知");
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
