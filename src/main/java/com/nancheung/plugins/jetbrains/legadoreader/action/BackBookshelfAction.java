package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderFactory;
import com.nancheung.plugins.jetbrains.legadoreader.toolwindow.ToolWindowReaderService;

public class BackBookshelfAction extends AnAction {
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        ((ToolWindowReaderService) ReaderFactory.TOOL_WINDOW.getReader()).backBookshelf();
    }
}
