package com.nancheung.plugins.jetbrains.legadoreader.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import com.nancheung.plugins.jetbrains.legadoreader.gui.SettingFactory;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
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
     * 正文面板的操作按钮bar
     */
    private JToolBar bar;
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
    
    private static final IndexUI INSTANCE = new IndexUI();
    
    
    public IndexUI() {
        // 初始化界面设置
        initIndexUI();
        
        // 初始化使用默认ip刷新书架目录
        refreshBookshelf(bookDTOS -> refreshBookshelfButton.setEnabled(true), throwable -> refreshBookshelfButton.setEnabled(true));
        
        // 刷新书架目录按钮事件
        refreshBookshelfButton.addActionListener(refreshBookshelfActionListener());
        // 表格数据点击事件
        bookshelfTable.addMouseListener(toTextBodyMouseAdapter());
        
        // ip输入框的历史记录点击事件
        addressHistoryBox.addItemListener(selectAddressHistoryItemListener());
    }
    
    public void refreshBookshelf(Consumer<List<BookDTO>> acceptConsumer, Consumer<Throwable> throwableConsumer) {
        // 调用API获取书架目录
        CompletableFuture.supplyAsync(ApiUtil::getBookshelf)
                .thenAccept(books -> {
                    // 保存书架目录信息
                    Data.setBookshelf(books);
                    // 设置书架目录UI
                    setBookshelfUI(books);
                    
                    acceptConsumer.accept(books);
                }).exceptionally(throwable -> {
                    showErrorTips(bookshelfScrollPane, bookshelfErrorTipsPane);
                    throwable.printStackTrace();
                    
                    throwableConsumer.accept(throwable);
                    return null;
                });
    }
    
    private void initIndexUI() {
        // 初始化设置数据
        SettingFactory.instance();
        
        // 隐藏正文面板
        textBodyPanel.setVisible(false);
        // 隐藏正文面板的错误提示
        textBodyErrorTipsPane.setVisible(false);
        
        // 隐藏书架面板的错误提示
        bookshelfErrorTipsPane.setVisible(false);
        // 设置书架面板的错误提示为不可编辑
        bookshelfErrorTipsPane.setEditable(false);
        
        // 设置书架面板的表格数据格式
        addressHistoryBox.setModel(ADDRESS_HISTORY_BOX_MODEL);
        
        // 设置书架面板的ip输入框及历史记录
        setAddressUI();
        
        // 设置书架面板的表格数据格式
        bookshelfTable.setModel(IndexUI.BOOK_SHELF_TABLE_MODEL);
        
        // 设置正文面板的错误提示为不可编辑
        textBodyErrorTipsPane.setEditable(false);
        
        // 创建action bar
        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar(Constant.PLUGIN_TOOL_BAR_ID, (DefaultActionGroup) actionManager.getAction(Constant.PLUGIN_TOOL_BAR_ID), true);
        
        // 将bar添加至ui
        bar.add(actionToolbar.getComponent());
        // 设置bar样式：无边框、不可拖动
        bar.setBorderPainted(false);
        bar.setFloatable(false);
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
    
    private MouseAdapter toTextBodyMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = bookshelfTable.rowAtPoint(evt.getPoint());
                int col = bookshelfTable.columnAtPoint(evt.getPoint());
                
                if (row < 0 || col < 0) {
                    return;
                }

                // 展示正文面板
                bookshelfPanel.setVisible(false);
                textBodyPanel.setVisible(true);
    
                TableModel model = ((JTable) evt.getSource()).getModel();
                String name = model.getValueAt(row, 0).toString();
                String author = model.getValueAt(row, 3).toString();
    
                // 保存当前阅读信息
                BookDTO book = Data.getBook(author, name);
                CurrentReadData.setBook(book);
                CurrentReadData.setBookIndex(book.getDurChapterIndex());
                
                // 调用API获取章节列表
                CompletableFuture.supplyAsync(() -> ApiUtil.getChapterList(CurrentReadData.getBook().getBookUrl()))
                        .thenAccept(bookChapters -> {
                            // 保存章节列表
                            CurrentReadData.setBookChapterList(bookChapters);
                            
                            switchChapter(book.getDurChapterPos());
                        }).exceptionally(throwable -> {
                            showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);
                            throwable.printStackTrace();
                            return null;
                        });
            }
        };
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
            bookshelfScrollPane.setVisible(true);
            bookshelfErrorTipsPane.setVisible(false);
        }
    }
    
    /**
     * 切换章节
     *
     * @param durChapterPos 当前在章节中的位置
     */
    public void switchChapter(int durChapterPos) {
        // 设置正文面板UI
        setTextBodyUI();
        
        // 设置加载中的提示
        textBodyPane.setText("加载中...");
        
        BookDTO book = CurrentReadData.getBook();
        
        // 获取章节标题
        String title = CurrentReadData.getBookChapter().getTitle();
        
        // 调用API获取正文内容
        CompletableFuture.supplyAsync(() -> ApiUtil.getBookContent(book.getBookUrl(), CurrentReadData.getBookIndex()))
                .thenAccept(bookContent -> {
                    CurrentReadData.setBodyContent(bookContent);
                    
                    // 保存阅读进度
                    ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), CurrentReadData.getBookIndex(), title, durChapterPos);
                    
                    // 设置正文内容
                    setTextBodyUIData(title, bookContent, durChapterPos);
                }).exceptionally(throwable -> {
                    showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);
                    throwable.printStackTrace();
                    return null;
                });
    }
    
    private void setTextBodyUI() {
        textBodyPane.setForeground(new JBColor(Data.textBodyFontColor, Data.textBodyFontColor));
        textBodyPane.setFont(Data.textBodyFont);
        
        if (!textBodyScrollPane.isShowing()) {
            textBodyScrollPane.setVisible(true);
            textBodyErrorTipsPane.setVisible(false);
        }
    
        // 获取焦点到文本框
        textBodyPane.requestFocus();
    }
    
    private void setTextBodyUIData(String title, String bookContent, int durChapterPos) {
        textBodyPane.setText(title + "\n" + bookContent);
        textBodyPane.setCaretPosition(durChapterPos);
    }
    
    private void showErrorTips(JScrollPane textBodyScrollPane, JTextPane textBodyErrorTipsPane) {
        textBodyScrollPane.setVisible(false);
        textBodyErrorTipsPane.setVisible(true);
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
    
    public JComponent getComponent() {
        return rootPanel;
    }
    
    public static IndexUI getInstance() {
        return INSTANCE;
    }
}
