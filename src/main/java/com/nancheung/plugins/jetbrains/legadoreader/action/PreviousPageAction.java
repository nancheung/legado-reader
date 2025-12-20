package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 上一页
 * 支持 ToolWindow 和 EditorLine 双模式
 */
@Slf4j
public class PreviousPageAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CommandBus.getInstance().dispatchAsync(Command.of(CommandType.PREVIOUS_PAGE));
        log.debug("翻到上一页");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 使用后台线程更新，因为只访问 Service，不访问 UI 组件
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 检查行内模式是否启用
        boolean enableShowBodyInLine = PluginSettingsStorage.getInstance()
                .getState().enableShowBodyInLine;

        // 如果行内模式被禁用，则禁用此 Action
        e.getPresentation().setEnabled(enableShowBodyInLine);
    }
}
