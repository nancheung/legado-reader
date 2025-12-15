package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderGlobalFacade;

/**
 * 下一章操作
 * 使用事件驱动架构，通过 Facade 发布事件，UI 订阅并更新
 */
public class NextChapterAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ReaderGlobalFacade.getInstance().nextChapter();
    }
}
