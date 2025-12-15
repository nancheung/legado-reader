package com.nancheung.plugins.jetbrains.legadoreader.toolwindow;

import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;

public class ToolWindowReaderService implements IReader {

    private final IndexUI indexUI = IndexUI.getInstance();

    @Override
    public void previousPage() {

    }

    @Override
    public void nextPage() {
    }

    @Override
    public void splitChapter(String chapterContent, int pageSize) {
    
    }
    
    public void backBookshelf() {
        indexUI.refreshBookshelf(bookDTOS -> {
        }, throwable -> {
        });
        
        indexUI.getTextBodyPanel().setVisible(false);
        indexUI.getBookshelfPanel().setVisible(true);
    }
}
