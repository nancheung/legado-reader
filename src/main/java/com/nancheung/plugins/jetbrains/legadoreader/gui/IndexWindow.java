package com.nancheung.plugins.jetbrains.legadoreader.gui;

import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndexWindow {
    /**
     * 主面板
     */
    @Getter
    private JPanel rootPanel;
    
    /**
     * 书架面板
     */
    private JPanel bookshelfPanel;
    /**
     * 书架面板的ip输入框
     */
    private JTextField ipTextField;
    /**
     * 书架面板的刷新按钮
     */
    private JButton refreshBookshelfButton;
    /**
     * 书架面板的目录的滚动面板
     */
    private JScrollPane bookshelfScrollPane;
    /**
     * 书架面板的目录表格
     */
    private JTable bookshelfTable;
    /**
     * 书架面板的错误提示
     */
    private JTextPane bookshelfErrorTipsPane;
    
    /**
     * 正文面板
     */
    private JPanel textBodyPanel;
    /**
     * 正文面板的标题
     */
    private JTextField titleField;
    /**
     * 正文面板的返回按钮
     */
    private JButton backButton;
    /**
     * 正文面板的下一章按钮
     */
    private JButton nextChapterButton;
    /**
     * 正文面板的上一章按钮
     */
    private JButton previousChapterButton;
    /**
     * 正文面板的滚动面板
     */
    private JScrollPane textBodyScrollPane;
    /**
     * 正文面板的滚动面板中的正文
     */
    private JTextPane textBodyPane;
    /**
     * 正文面板的错误提示
     */
    private JTextPane textBodyErrorTipsPane;
    
    private static final String[] BOOK_SHELF_COLUMN_NAME = {"name", "current", "new", "author"};
    
    public static DefaultTableModel BOOK_SHELF_TABLE_MODEL = new DefaultTableModel(null, BOOK_SHELF_COLUMN_NAME) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // 表格不允许被编辑
            return false;
        }
    };
    
    public IndexWindow() {
        // 初始化界面设置
        initIndexUI();
        
        // 初始化使用默认ip刷新书架目录
        Data.IP = ipTextField.getText();
        refreshBookshelf();
        
        // 刷新书架目录按钮事件
        refreshBookshelfButton.addActionListener(refreshBookshelfActionListener());
        // 表格数据点击事件
        bookshelfTable.addMouseListener(toTextBodyMouseAdapter());
        // 返回书架按钮事件
        backButton.addActionListener(backBookshelfActionListener());
        
        // 上一章按钮事件
        previousChapterButton.addActionListener(previousChapterActionListener());
        // 下一章按钮事件
        nextChapterButton.addActionListener(nextChapterActionListener());
    }
    
    private void initIndexUI() {
        // 隐藏正文面板
        textBodyPanel.hide();
        // 隐藏正文面板的错误提示
        textBodyErrorTipsPane.hide();
        // 设置正文面板的字体颜色
        textBodyPane.setForeground(new JBColor(new Color(95, 158, 160), new Color(95,158 ,160)));
        // 设置正文面板的错误提示为不可编辑
        textBodyErrorTipsPane.setEditable(false);
        // 设置正文面板的标题为不可编辑
        titleField.setEditable(false);
        
        // 隐藏书架面板的错误提示
        bookshelfErrorTipsPane.hide();
        // 设置书架面板的错误提示为不可编辑
        bookshelfErrorTipsPane.setEditable(false);
        
        // 设置书架面板的表格数据格式
        bookshelfTable.setModel(IndexWindow.BOOK_SHELF_TABLE_MODEL);
    }
    
    private ActionListener previousChapterActionListener() {
        return e -> {
            // 第一章无法继续上一章
            if (Data.currentBookIndex < 1) {
                return;
            }
            
            // 更新索引
            Data.currentBookIndex--;
            
            switchChapter();
        };
    }
    
    private ActionListener nextChapterActionListener() {
        return e -> {
            // 更新索引
            Data.currentBookIndex++;
            
            switchChapter();
        };
    }
    
    private ActionListener backBookshelfActionListener() {
        return e -> {
            refreshBookshelf();
            
            textBodyPanel.hide();
            bookshelfPanel.show();
        };
    }
    
    private ActionListener refreshBookshelfActionListener() {
        return e -> {
            Data.IP = ipTextField.getText();
            
            refreshBookshelf();
        };
    }
    
    private void refreshBookshelf() {
        try {
            // 调用API获取书架目录
            List<BookDTO> books = ApiUtil.getBookshelf();
            // 保存书架目录信息
            Data.bookshelf = books.stream().collect(Collectors.toMap(book -> book.getAuthor() + "#" + book.getName(), Function.identity()));
            
            setBookshelfUI(books);
        } catch (Exception e) {
            showErrorTips(bookshelfScrollPane, bookshelfErrorTipsPane);
        }
    }
    
    private void setBookshelfUI(List<BookDTO> books) {
        // 清空表格
        IndexWindow.BOOK_SHELF_TABLE_MODEL.getDataVector().clear();
        
        // 添加表格数据
        books.stream().map(book -> {
            Vector<String> bookVector = new Vector<>();
            bookVector.add(book.getName());
            bookVector.add(book.getDurChapterTitle());
            bookVector.add(book.getLatestChapterTitle());
            bookVector.add(book.getAuthor());
            return bookVector;
        }).forEach(data -> IndexWindow.BOOK_SHELF_TABLE_MODEL.addRow(data));
        
        if (!bookshelfScrollPane.isShowing()) {
            bookshelfScrollPane.show();
            bookshelfErrorTipsPane.hide();
        }
    }
    
    private MouseAdapter toTextBodyMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = bookshelfTable.rowAtPoint(evt.getPoint());
                int col = bookshelfTable.columnAtPoint(evt.getPoint());
                
                if (row < 0 || col < 0) {
                    return;
                }
                
                TableModel model = ((JTable) evt.getSource()).getModel();
                String name = model.getValueAt(row, 0).toString();
                String author = model.getValueAt(row, 3).toString();
                
                // 保存当前阅读信息
                Data.currentBook = Data.getBook(author, name);
                Data.currentBookIndex = Data.currentBook.getDurChapterIndex();
                // 调用API获取并保存章节列表
                try {
                    Data.currentBookChapterList = ApiUtil.getChapterList(Data.currentBook.getBookUrl());
                } catch (Exception e) {
                    showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);
                }
                
                switchChapter();
                
                bookshelfPanel.hide();
                textBodyPanel.show();
            }
        };
    }
    
    private void setTextBodyUI(String name, String title, String bookContent) {
        titleField.setText(name + " - " + title);
        textBodyPane.setText(bookContent);
        
        if (!textBodyScrollPane.isShowing()) {
            textBodyScrollPane.show();
            textBodyErrorTipsPane.hide();
        }
    }
    
    private void switchChapter() {
        try {
            // 调用API获取正文内容
            String bookContent = ApiUtil.getBookContent(Data.currentBook.getBookUrl(), Data.currentBookIndex);
            
            // 获取章节标题
            String title = Data.currentBookChapterList.get(Data.currentBookIndex).getTitle();
            
            // 保存阅读进度
            ApiUtil.saveBookProgress(Data.currentBook.getAuthor(), Data.currentBook.getName(), Data.currentBookIndex, title);
            
            // 设置正文内容
            setTextBodyUI(Data.currentBook.getName(), title, bookContent);
        } catch (Exception e) {
            showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);
        }
    }
    
    private void showErrorTips(JScrollPane textBodyScrollPane, JTextPane textBodyErrorTipsPane) {
        textBodyScrollPane.hide();
        textBodyErrorTipsPane.show();
    }
    
    
}
