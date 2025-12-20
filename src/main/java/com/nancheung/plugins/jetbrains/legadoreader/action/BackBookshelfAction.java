package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.toolwindow.IndexUI;
import org.jetbrains.annotations.NotNull;

public class BackBookshelfAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        backBookshelf();
    }

    private void backBookshelf() {
        IndexUI indexUI = IndexUI.getInstance();
        indexUI.refreshBookshelf(bookDTOS -> {
        }, throwable -> {
        });

        indexUI.getTextBodyPanel().setVisible(false);
        indexUI.getBookshelfPanel().setVisible(true);
    }
}
