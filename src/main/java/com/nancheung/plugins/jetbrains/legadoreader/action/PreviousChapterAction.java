package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderGlobalFacade;

public class PreviousChapterAction extends AnAction {
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        ReaderGlobalFacade.getInstance().previousChapter();
    }
}
