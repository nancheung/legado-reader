package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 切换阅读模式显示/隐藏
 * 全局开关，同时影响 ToolWindow 和 EditorLine 两种阅读模式
 */
@Slf4j
public class ToggleReadingModeAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 切换阅读模式状态
        PluginSettingsStorage storage = PluginSettingsStorage.getInstance();
        boolean newState = storage.toggleReadingMode();

        log.info("阅读模式已{}", newState ? "启用" : "禁用");

        // 刷新编辑器显示
        refreshEditor();

    }

    /**
     * 刷新所有打开的编辑器
     * 在 EDT 线程中执行，触发行内内容重绘
     */
    private void refreshEditor() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

            for (Project project : openProjects) {
                if (project != null && !project.isDisposed()) {
                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    if (editor != null && !editor.isDisposed()) {
                        // 触发编辑器内容组件重绘
                        editor.getContentComponent().repaint();
                        log.debug("已刷新项目 {} 的编辑器", project.getName());
                    }
                }
            }

        });
    }

}
