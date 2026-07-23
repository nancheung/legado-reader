package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 阅读窗口 显示/隐藏
 * 窗口可见时隐藏，隐藏时重新打开并聚焦
 */
@Slf4j
public class ToggleToolWindowAction extends AnAction {

    /**
     * ToolWindow ID，必须与 plugin.xml 中 toolWindow 的 id 一致
     */
    private static final String TOOL_WINDOW_ID = "Legado Reader";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) {
            log.warn("未找到 ToolWindow: {}", TOOL_WINDOW_ID);
            return;
        }

        if (toolWindow.isVisible()) {
            toolWindow.hide(null);
        } else {
            toolWindow.activate(null);
        }
    }
}
