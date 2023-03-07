package com.nancheung.plugins.jetbrains.legadoreader.gui.ui;

import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import com.nancheung.plugins.jetbrains.legadoreader.gui.SettingFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndexUI {
    /**
     * 主面板
     */
    private JPanel rootPanel;
    
    /**
     * 书架面板
     */
    private JPanel bookshelfPanel;
    /**
     * 书架面板的ip输入框
     */
    private JTextField addressTextField;
    /**
     * 书架面板的ip输入框的历史记录
     */
    private JComboBox<String> addressHistoryBox;
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
    private JLabel titleLabel;
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
    
    private static final DefaultTableModel BOOK_SHELF_TABLE_MODEL = new DefaultTableModel(null, new String[]{"name", "current", "new", "author"}) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // 表格不允许被编辑
            return false;
        }
    };
    
    public static final DefaultComboBoxModel<String> ADDRESS_HISTORY_BOX_MODEL = new DefaultComboBoxModel<>();
    
    
    public IndexUI() {
        // 初始化界面设置
        initIndexUI();
        
        // 初始化使用默认ip刷新书架目录
        refreshBookshelf(bookDTOS -> refreshBookshelfButton.setEnabled(true), throwable -> refreshBookshelfButton.setEnabled(true));
        
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
        // ip输入框的历史记录点击事件
        addressHistoryBox.addItemListener(selectAddressHistoryItemListener());
    }
    
    private void initIndexUI() {
        // 初始化设置数据
        SettingFactory.instance();
        
        // 隐藏正文面板
        textBodyPanel.hide();
        // 隐藏正文面板的错误提示
        textBodyErrorTipsPane.hide();
        // 设置正文面板的错误提示为不可编辑
        textBodyErrorTipsPane.setEditable(false);
        
        // 隐藏书架面板的错误提示
        bookshelfErrorTipsPane.hide();
        // 设置书架面板的错误提示为不可编辑
        bookshelfErrorTipsPane.setEditable(false);
        
        // 设置书架面板的表格数据格式
        addressHistoryBox.setModel(ADDRESS_HISTORY_BOX_MODEL);
        
        // 设置书架面板的ip输入框及历史记录
        setAddressUI();
        
        // 设置书架面板的表格数据格式
        bookshelfTable.setModel(IndexUI.BOOK_SHELF_TABLE_MODEL);
    }
    
    private ActionListener previousChapterActionListener() {
        return e -> {
            // 第一章无法继续上一章
            if (CurrentReadData.getBookIndex() < 1) {
                return;
            }
            
            // 设置按钮不可点击，防止多次点击
            previousChapterButton.setEnabled(false);
            
            // 设置加载中的提示
            titleLabel.setText("加载中...");
            textBodyPane.setText("加载中...");
            
            // 更新索引
            CurrentReadData.indexAtomicDecrement();
            
            switchChapter(bookDTOS -> previousChapterButton.setEnabled(true), throwable -> previousChapterButton.setEnabled(true));
        };
    }
    
    private ActionListener nextChapterActionListener() {
        return e -> {
            // 设置按钮不可点击，防止多次点击
            nextChapterButton.setEnabled(false);
            
            // 设置加载中的提示
            titleLabel.setText("加载中...");
            textBodyPane.setText("加载中...");
            
            // 更新索引
            CurrentReadData.indexAtomicIncrement();
            
            switchChapter(bookDTOS -> nextChapterButton.setEnabled(true), throwable -> nextChapterButton.setEnabled(true));
        };
    }
    
    private ActionListener backBookshelfActionListener() {
        return e -> {
            // 设置按钮不可点击，防止多次点击
            backButton.setEnabled(false);
            
            refreshBookshelf(bookDTOS -> backButton.setEnabled(true), throwable -> backButton.setEnabled(true));
            
            
            textBodyPanel.hide();
            bookshelfPanel.show();
        };
    }
    
    private ActionListener refreshBookshelfActionListener() {
        return e -> {
            // 设置按钮不可点击，防止多次点击
            refreshBookshelfButton.setEnabled(false);
            
            Data.addAddress(addressTextField.getText());
            
            setAddressUI();
            
            refreshBookshelf(bookDTOS -> refreshBookshelfButton.setEnabled(true), throwable -> refreshBookshelfButton.setEnabled(true));
        };
    }
    
    private ItemListener selectAddressHistoryItemListener() {
        return e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                addressTextField.setText(e.getItem().toString());
            }
        };
    }
    
    private void refreshBookshelf(Consumer<List<BookDTO>> acceptConsumer, Consumer<Throwable> throwableConsumer) {
        // 调用API获取书架目录
        CompletableFuture.supplyAsync(ApiUtil::getBookshelf)
                .thenAccept(books -> {
                    // 保存书架目录信息
                    Data.bookshelf = books.stream().collect(Collectors.toMap(book -> book.getAuthor() + "#" + book.getName(), Function.identity()));
                    
                    setBookshelfUI(books);
                    
                    acceptConsumer.accept(books);
                }).exceptionally(throwable -> {
                    showErrorTips(bookshelfScrollPane, bookshelfErrorTipsPane);
                    throwable.printStackTrace();
                    
                    throwableConsumer.accept(throwable);
                    return null;
                });
    }
    
    private void setBookshelfUI(List<BookDTO> books) {
        // 清空表格
        IndexUI.BOOK_SHELF_TABLE_MODEL.getDataVector().clear();
        
        // 添加表格数据
        books.stream().map(book -> {
            Vector<String> bookVector = new Vector<>();
            bookVector.add(book.getName());
            bookVector.add(book.getDurChapterTitle());
            bookVector.add(book.getLatestChapterTitle());
            bookVector.add(book.getAuthor());
            return bookVector;
        }).forEach(IndexUI.BOOK_SHELF_TABLE_MODEL::addRow);
        
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
                BookDTO book = Data.getBook(author, name);
                CurrentReadData.setBook(book);
                CurrentReadData.setBookIndex(book.getDurChapterIndex());
                
                // 展示正文面板
                bookshelfPanel.hide();
                textBodyPanel.show();
                
                // 设置按钮不可点击，防止多次点击
                previousChapterButton.setEnabled(false);
                nextChapterButton.setEnabled(false);
                
                // 设置加载中的提示
                titleLabel.setText("加载中...");
                textBodyPane.setText("加载中...");
                
                // 调用API获取章节列表
                CompletableFuture.supplyAsync(() -> ApiUtil.getChapterList(CurrentReadData.getBook().getBookUrl()))
                        .thenAccept(bookChapters -> {
                            // 保存章节列表
                            CurrentReadData.setBookChapterList(bookChapters);
                            
                            switchChapter(bookDTOS -> {
                                previousChapterButton.setEnabled(true);
                                nextChapterButton.setEnabled(true);
                            }, throwable -> {
                                previousChapterButton.setEnabled(true);
                                nextChapterButton.setEnabled(true);
                            });
                        }).exceptionally(throwable -> {
                            showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);
                            throwable.printStackTrace();
                            return null;
                        });
            }
        };
    }
    
    private void switchChapter(Consumer<String> acceptConsumer, Consumer<Throwable> throwableConsumer) {
        BookDTO book = CurrentReadData.getBook();
        
        // 获取章节标题
        String title = CurrentReadData.getBookChapter().getTitle();
        
        // 调用API获取正文内容
        CompletableFuture.supplyAsync(() -> ApiUtil.getBookContent(book.getBookUrl(), CurrentReadData.getBookIndex()))
                .thenAccept(bookContent -> {
                    // 保存阅读进度
                    ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), CurrentReadData.getBookIndex(), title);
                    
                    // 设置正文内容
                    setTextBodyUI(book.getName(), title, bookContent);
                    
                    acceptConsumer.accept(bookContent);
                }).exceptionally(throwable -> {
                    showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);
                    throwable.printStackTrace();
                    
                    throwableConsumer.accept(throwable);
                    return null;
                });
    }
    
    private void setTextBodyUI(String name, String title, String bookContent) {
        titleLabel.setText(name + " - " + title);
        textBodyPane.setText(bookContent);
        textBodyPane.setForeground(new JBColor(Data.textBodyFontColor, Data.textBodyFontColor));
        textBodyPane.setCaretPosition(0);
        textBodyPane.setFont(Data.textBodyFont);
        
        if (!textBodyScrollPane.isShowing()) {
            textBodyScrollPane.show();
            textBodyErrorTipsPane.hide();
        }
    }
    
    private void showErrorTips(JScrollPane textBodyScrollPane, JTextPane textBodyErrorTipsPane) {
        textBodyScrollPane.hide();
        textBodyErrorTipsPane.show();
    }
    
    public JComponent getComponent() {
        return rootPanel;
    }
    
    private void setAddressUI() {
        List<String> addressHistoryList = Data.getAddressHistory();
        // 设置书架面板的ip输入框的历史记录
        ADDRESS_HISTORY_BOX_MODEL.removeAllElements();
        ADDRESS_HISTORY_BOX_MODEL.addAll(addressHistoryList);
        
        if (addressHistoryList.size() == 0) {
            addressHistoryBox.setEnabled(false);
            ADDRESS_HISTORY_BOX_MODEL.addElement("无历史记录");
            return;
        }
        
        // 设置书架面板的ip输入框
        addressHistoryBox.setEnabled(true);
        ADDRESS_HISTORY_BOX_MODEL.setSelectedItem(addressHistoryList.get(0));
        addressTextField.setText(addressHistoryList.get(0));
        
    }
    
    public void setTextBodyFontColor(Color color) {
        textBodyPane.setForeground(new JBColor(color, color));
    }
    
    public void setTextBodyFont(Font textBodyFont) {
        textBodyPane.setFont(textBodyFont);
    }
}
