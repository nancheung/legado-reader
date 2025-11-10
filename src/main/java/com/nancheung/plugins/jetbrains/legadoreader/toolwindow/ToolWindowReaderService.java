package com.nancheung.plugins.jetbrains.legadoreader.toolwindow;

import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;

public class ToolWindowReaderService implements IReader {

    private final IndexUI indexUI = IndexUI.getInstance();

    @Override
    public void previousPage() {

    }

    @Override
    public void nextPage() {
    }

    @Override
    public void previousChapter() {
        // 第一章无法继续上一章
        if (ReadingSessionManager.getInstance().getCurrentChapterIndex() < 1) {
            return;
        }

        // 更新索引
        ReadingSessionManager.getInstance().previousChapter();


        indexUI.switchChapter(0);
    }

    @Override
    public void nextChapter() {
        // 更新索引
        ReadingSessionManager.getInstance().nextChapter();

        indexUI.switchChapter(0);
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
