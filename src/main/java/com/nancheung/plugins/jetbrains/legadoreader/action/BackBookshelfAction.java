package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import org.jetbrains.annotations.NotNull;

/**
 * 返回书架
 * 走指令链路结束阅读会话（冲刷待同步进度、清空会话），UI 响应 SESSION_ENDED 事件刷新并显示书架
 */
public class BackBookshelfAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CommandBus.getInstance().dispatchAsync(Command.of(CommandType.BACK_TO_BOOKSHELF));
    }
}
