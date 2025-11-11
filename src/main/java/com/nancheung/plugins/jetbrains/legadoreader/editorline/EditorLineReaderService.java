package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;
import com.nancheung.plugins.jetbrains.legadoreader.manager.BodyInLineDataManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 编辑器行内阅读服务
 * 实现 IReader 接口，提供翻页等阅读操作
 */
@Slf4j
public class EditorLineReaderService implements IReader {

    /**
     * 上一页
     */
    @Override
    public void previousPage() {
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        BodyInLineDataManager.LineData currentLine = dataManager.getCurrentLine();

        if (currentLine == null) {
            log.debug("当前页为空，无法翻到上一页");
            return;
        }

        List<BodyInLineDataManager.LineData> lineContentList = dataManager.getLineContentList();
        if (lineContentList == null || lineContentList.isEmpty()) {
            log.warn("分页列表为空");
            return;
        }

        int currentLineIndex = currentLine.getLineIndex();

        // 如果不是第一页，则翻到上一页
        if (currentLineIndex > 0) {
            dataManager.setCurrentLine(lineContentList.get(currentLineIndex - 1));
            log.debug("翻到上一页: " + (currentLineIndex));

            // 触发 UI 刷新
            refreshEditor();
        } else {
            log.debug("已经是第一页，无法继续翻页");
        }

        // 判断当前行小于总行数的50%，则可以预载上一章
        if (currentLineIndex < lineContentList.size() * DEFAULT_LOAD_FACTOR) {
            // TODO: 预载上一章（暂未实现）
            log.debug("可以预载上一章（功能未实现）");
        }


    }

    /**
     * 下一页
     */
    @Override
    public void nextPage() {
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        BodyInLineDataManager.LineData currentLine = dataManager.getCurrentLine();

        if (currentLine == null) {
            log.debug("当前页为空，无法翻到下一页");
            return;
        }

        List<BodyInLineDataManager.LineData> lineContentList = dataManager.getLineContentList();
        if (lineContentList == null || lineContentList.isEmpty()) {
            log.warn("分页列表为空");
            return;
        }

        int currentLineIndex = currentLine.getLineIndex();

        // 如果不是最后一页，则翻到下一页
        if (currentLineIndex < lineContentList.size() - 1) {
            dataManager.setCurrentLine(lineContentList.get(currentLineIndex + 1));
            log.debug("翻到下一页: " + (currentLineIndex + 2));

            // 触发 UI 刷新
            refreshEditor();
        } else {
            log.debug("已经是最后一页，无法继续翻页");
        }

        // 判断当前行大于总行数的50%，则可以预载下一章
        if (currentLineIndex > lineContentList.size() * DEFAULT_LOAD_FACTOR) {
            // TODO: 预载下一章（暂未实现）
            log.debug("可以预载下一章（功能未实现）");
        }


    }

    /**
     * 上一章
     * 注意：此功能暂未实现，仅保留接口
     */
    @Override
    public void previousChapter() {
        log.warn("上一章功能暂未实现");
        // TODO: 实现上一章切换逻辑
        // 1. 从 ReadingSessionManager 获取上一章信息
        // 2. 调用 API 获取上一章内容
        // 3. 更新 ReadingSessionManager 和 BodyInLineDataManager
        // 4. 刷新 UI
    }

    /**
     * 下一章
     * 注意：此功能暂未实现，仅保留接口
     */
    @Override
    public void nextChapter() {
        log.warn("下一章功能暂未实现");
        // TODO: 实现下一章切换逻辑
        // 1. 从 ReadingSessionManager 获取下一章信息
        // 2. 调用 API 获取下一章内容
        // 3. 更新 ReadingSessionManager 和 BodyInLineDataManager
        // 4. 刷新 UI
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
