package com.nancheung.plugins.jetbrains.legadoreader.gui;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.AddressEnum;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.R;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

public class IndexWindow {
    private JTextField ipTextField;
    private JButton jumpButton;
    
    
    @Getter
    private JPanel root;
    private JTable bookshelfTable;
    
    public static String IP;
    
    private static String[] BOOK_SHELF_COLUMN_NAME = {"书名", "已读", "最新", "作者", "url", "索引"};
    
    public static DefaultTableModel BOOK_SHELF_TABLE_MODEL = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    public IndexWindow() {
        IndexWindow.BOOK_SHELF_TABLE_MODEL.setColumnIdentifiers(IndexWindow.BOOK_SHELF_COLUMN_NAME);
        bookshelfTable.setModel(IndexWindow.BOOK_SHELF_TABLE_MODEL);
        
        // 跳转按钮事件
        jumpButton.addActionListener(e -> {
            String ip = ipTextField.getText();
            IndexWindow.IP = ip;
            
            // 调用API获取书架目录
            String url = ip + AddressEnum.GET_BOOKSHELF.getAddress();
            String textBody = HttpUtil.get(url);
            R<List<BookDTO>> books = JSONUtil.toBean(textBody, new TypeReference<>() {
            }, true);
            
//            System.out.println(url + "\n" + textBody);
            
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
    
            if (bookshelfTable.getColumnCount()>4) {
                bookshelfTable.removeColumn(bookshelfTable.getColumnModel().getColumn(5));
                bookshelfTable.removeColumn(bookshelfTable.getColumnModel().getColumn(4));
            }
      
        });
    
        // 表格数据点击事件
        bookshelfTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount()!=2) {
                    return;
                }
    
                int row = bookshelfTable.rowAtPoint(evt.getPoint());
                int col = bookshelfTable.columnAtPoint(evt.getPoint());
                

                if (row >= 0 && col >= 0) {
                    TableModel model = ((JTable) evt.getSource()).getModel();
                    Object valueAt = model.getValueAt(row, col);
                    String bookUrl = model.getValueAt(row, 4).toString();
                    String index = model.getValueAt(row, 5).toString();
                    
                    // 调用API获取书架目录
                    String url = IndexWindow.IP + AddressEnum.GET_BOOK_CONTENT.getAddress()+ "?url=" + URLUtil.encode(bookUrl + "&index=" + index);
                    String textBody = HttpUtil.get(url);
                    R<String> bookContent = JSONUtil.toBean(textBody, new TypeReference<>() {
                    }, true);
                    
    
                    System.out.println("双击row: " + row + ", col: " + col + "。" + valueAt + "\n" + bookContent.getData());
                }
            }
        });
    }
}
