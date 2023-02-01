package com.nancheung.plugins.jetbrains.legadoreader.gui;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.AddressEnum;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.R;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

public class IndexWindow {
    @Getter
    private JPanel rootPanel;
    private JTextField ipTextField;
    private JButton jumpButton;
    private JTable bookshelfTable;
    private JTextPane textBodyPane;
    private JPanel textBodyPanel;
    private JButton backButton;
    private JTextField titleField;
    private JButton nextChapterButton;
    private JButton previousChapterButton;
    private JPanel bookshelfPanel;
    
    public static String IP;
    
    private static final String[] BOOK_SHELF_COLUMN_NAME = {"书名", "已读", "最新", "作者", "url", "索引"};
    
    public static DefaultTableModel BOOK_SHELF_TABLE_MODEL = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            // 表格不允许被编辑
            return false;
        }
    };
    
    public IndexWindow() {
        // 初始化
        textBodyPanel.hide();
        IndexWindow.BOOK_SHELF_TABLE_MODEL.setColumnIdentifiers(IndexWindow.BOOK_SHELF_COLUMN_NAME);
        bookshelfTable.setModel(IndexWindow.BOOK_SHELF_TABLE_MODEL);
        
        // 查看书架目录按钮事件
        jumpButton.addActionListener(getBookshelfActionListener());
        // 表格数据点击事件
        bookshelfTable.addMouseListener(toTextBodyMouseAdapter());
        // 返回书架按钮事件
        backButton.addActionListener(backBookshelfActionListener());
    }
    
    private ActionListener backBookshelfActionListener() {
        return e -> {
            textBodyPanel.hide();
            bookshelfPanel.show();
        };
    }
    
    private ActionListener getBookshelfActionListener() {
        return e -> {
            String ip = ipTextField.getText();
            IndexWindow.IP = ip;
            
            // 调用API获取书架目录
            String url = ip + AddressEnum.GET_BOOKSHELF.getAddress();
            String textBody = HttpUtil.get(url);
            R<List<BookDTO>> books = JSONUtil.toBean(textBody, new TypeReference<>() {
            }, true);
            
            // 清空表格
            IndexWindow.BOOK_SHELF_TABLE_MODEL.getDataVector().clear();
            
            // 添加表格数据
            books.getData().stream().map(book -> {
                Vector<String> bookVector = new Vector<>();
                bookVector.add(book.getName());
                bookVector.add(book.getDurChapterTitle());
                bookVector.add(book.getLatestChapterTitle());
                bookVector.add(book.getAuthor());
                bookVector.add(book.getBookUrl());
                bookVector.add(String.valueOf(book.getDurChapterIndex()));
                return bookVector;
            }).forEach(data -> IndexWindow.BOOK_SHELF_TABLE_MODEL.addRow(data));
            
            // 删除不展示的列
            if (bookshelfTable.getColumnCount() > 4) {
                bookshelfTable.removeColumn(bookshelfTable.getColumnModel().getColumn(5));
                bookshelfTable.removeColumn(bookshelfTable.getColumnModel().getColumn(4));
            }
            
        };
    }
    
    private MouseAdapter toTextBodyMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = bookshelfTable.rowAtPoint(evt.getPoint());
                int col = bookshelfTable.columnAtPoint(evt.getPoint());
                
                
                if (row >= 0 && col >= 0) {
                    TableModel model = ((JTable) evt.getSource()).getModel();

                    String name = model.getValueAt(row, 0).toString();
                    String bookUrl = model.getValueAt(row, 4).toString();
                    String index = model.getValueAt(row, 5).toString();
                    
                    // 调用API获取书架目录
                    String url = IndexWindow.IP + AddressEnum.GET_BOOK_CONTENT.getAddress() + "?url=" + URLUtil.encode(bookUrl + "&index=" + index);
                    String textBody = HttpUtil.get(url);
                    R<String> bookContent = JSONUtil.toBean(textBody, new TypeReference<>() {
                    }, true);
                    
                    // 切换到阅读界面
                    titleField.setText(name);
                    textBodyPane.setText(bookContent.getData());
                    bookshelfPanel.hide();
                    textBodyPanel.show();
                }
            }
        };
    }
}
