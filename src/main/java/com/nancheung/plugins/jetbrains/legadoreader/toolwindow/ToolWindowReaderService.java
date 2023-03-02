package com.nancheung.plugins.jetbrains.legadoreader.toolwindow;

import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
        if (CurrentReadData.getBookIndex() < 1) {
            return;
        }
    
        // 设置按钮不可点击，防止多次点击
        indexUI.getPreviousChapterButton().setEnabled(false);
    
        // 设置加载中的提示
        indexUI.getTitleLabel().setText("加载中...");
        indexUI.getTextBodyPane().setText("加载中...");
    
        // 更新索引
        CurrentReadData.indexAtomicDecrement();
    
        switchChapter(bookDTOS -> indexUI.getPreviousChapterButton().setEnabled(true), throwable -> indexUI.getPreviousChapterButton().setEnabled(true));
    }
    
    @Override
    public void nextChapter() {
        // 设置按钮不可点击，防止多次点击
        JButton nextChapterButton = indexUI.getNextChapterButton();
        JLabel titleLabel = indexUI.getTitleLabel();
        JTextPane textBodyPane = indexUI.getTextBodyPane();
        
        nextChapterButton.setEnabled(false);
    
        // 设置加载中的提示
        titleLabel.setText("加载中...");
        textBodyPane.setText("加载中...");
    
        // 更新索引
        CurrentReadData.indexAtomicIncrement();
    
        switchChapter(bookDTOS -> nextChapterButton.setEnabled(true), throwable -> nextChapterButton.setEnabled(true));
    }
    
    @Override
    public void splitChapter(String chapterContent, int pageSize) {
    
    }
    
    private void switchChapter(Consumer<String> acceptConsumer, Consumer<Throwable> throwableConsumer) {
        BookDTO book = CurrentReadData.getBook();
        
        // 获取章节标题
        String title = CurrentReadData.getBookChapter().getTitle();
        
        // 调用API获取正文内容
        CompletableFuture.supplyAsync(() -> ApiUtil.getBookContent(book.getBookUrl(), CurrentReadData.getBookIndex()))
                .thenAccept(bookContent -> {
                    CurrentReadData.setBodyContent(bookContent);
                    
                    // 保存阅读进度
                    ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), CurrentReadData.getBookIndex(), title);
                    
                    // 设置正文内容
                    setTextBodyUI(book.getName(), title, bookContent);
                    
                    acceptConsumer.accept(bookContent);
                }).exceptionally(throwable -> {
                    showErrorTips(indexUI.getTextBodyScrollPane(), indexUI.getTextBodyErrorTipsPane());
                    throwable.printStackTrace();
                    
                    throwableConsumer.accept(throwable);
                    return null;
                });
    }
    
    private void setTextBodyUI(String name, String title, String bookContent) {
        indexUI.getTitleLabel().setText(name + " - " + title);
        JTextPane textBodyPane = indexUI.getTextBodyPane();
        textBodyPane.setText(bookContent);
        textBodyPane.setForeground(new JBColor(Data.textBodyFontColor, Data.textBodyFontColor));
        textBodyPane.setCaretPosition(0);
        textBodyPane.setFont(Data.textBodyFont);
        
        if (!indexUI.getTextBodyScrollPane().isShowing()) {
            indexUI.getTextBodyScrollPane().show();
            indexUI.getTextBodyErrorTipsPane().hide();
        }
    }
    
    private void showErrorTips(JScrollPane textBodyScrollPane, JTextPane textBodyErrorTipsPane) {
        textBodyScrollPane.hide();
        textBodyErrorTipsPane.show();
    }
}
