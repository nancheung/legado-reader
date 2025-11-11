package com.nancheung.plugins.jetbrains.legadoreader.action;

import cn.hutool.core.collection.CollUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import org.jetbrains.annotations.NotNull;

public class ShowBookInfoAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 使用后台线程更新，因为只访问 Service，不访问 UI 组件
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        BookDTO book = ReadingSessionManager.getInstance().getCurrentBook();

        if (book == null || CollUtil.isEmpty(ReadingSessionManager.getInstance().getChapters())) {
            return;
        }

        // 将阅读信息设置到action的描述中
        String text = book.getName() + " - " + ReadingSessionManager.getInstance().getCurrentChapter().getTitle();
        e.getPresentation().setText(text);
    }
}
