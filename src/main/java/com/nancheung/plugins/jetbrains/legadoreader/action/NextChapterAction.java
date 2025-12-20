package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import org.jetbrains.annotations.NotNull;

/**
 * 下一章操作
 * 使用事件驱动架构，通过 CommandBus 发布事件，UI 订阅并更新
 */
public class NextChapterAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CommandBus.getInstance().dispatchAsync(Command.of(CommandType.NEXT_CHAPTER));
    }
}
