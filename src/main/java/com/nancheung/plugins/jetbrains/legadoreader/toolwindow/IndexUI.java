package com.nancheung.plugins.jetbrains.legadoreader.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.gui.SettingFactory;
import com.nancheung.plugins.jetbrains.legadoreader.manager.AddressHistoryManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class IndexUI {

    private static final Logger log = Logger.getInstance(IndexUI.class);


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

    /**
     * 单例实例，延迟初始化避免类加载时访问 Service
     */
    private static IndexUI INSTANCE;

    /**
     * 书架目录（临时存储在内存）
     * key: author + "#" + name
     * value: 书籍信息
     */
    private Map<String, BookDTO> bookshelf;

    private static final BiFunction<String, String, String> BOOK_MAP_KEY_FUNC = (author, name) -> author + "#" + name;


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
                    this.bookshelf = books.stream()
                            .collect(Collectors.toMap(
                                    book -> BOOK_MAP_KEY_FUNC.apply(book.getAuthor(), book.getName()),
                                    Function.identity()
                            ));
                    // 设置书架目录UI
                    setBookshelfUI(books);

                    acceptConsumer.accept(books);
                }).exceptionally(throwable -> {
                    showErrorTips(bookshelfScrollPane, bookshelfErrorTipsPane);

                    if (PluginSettingsManager.getInstance().isEnableErrorLog()) {
                        log.error("获取书架列表失败", throwable.getCause());
                    }

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

        // 注意：不在初始化时调用 setAddressUI()，避免访问 Service
        // 将在 initAddressHistory() 中延迟调用

        // 设置书架面板的表格数据格式
        bookshelfTable.setModel(IndexUI.BOOK_SHELF_TABLE_MODEL);

        // 设置正文面板的错误提示为不可编辑
        textBodyErrorTipsPane.setEditable(false);

        // 创建action bar
        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar(Constant.PLUGIN_TOOL_BAR_ID, (DefaultActionGroup) actionManager.getAction(Constant.PLUGIN_TOOL_BAR_ID), true);

        // 显式设置 TargetComponent，确保 actions 在正确的上下文中更新
        actionToolbar.setTargetComponent(textBodyPane);

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

            AddressHistoryManager.getInstance().addAddress(addressTextField.getText());

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

                // 设置正文面板UI
                initTextBodyUI();

                // 获取当前点击的书籍信息
                TableModel model = ((JTable) evt.getSource()).getModel();
                String name = model.getValueAt(row, 0).toString();
                String author = model.getValueAt(row, 3).toString();

                // 获取书籍信息
                BookDTO book = getBook(author, name);

                // 调用API获取章节列表
                CompletableFuture.supplyAsync(() -> ApiUtil.getChapterList(book.getBookUrl()))
                        .thenAccept(bookChapters -> {
                            // 创建新的 ReadingSession
                            ReadingSession session = new ReadingSession(
                                    book,
                                    bookChapters,
                                    book.getDurChapterIndex(),
                                    null  // content 初始为 null
                            );
                            ReadingSessionManager.getInstance().setSession(session);

                            // 设置正文数据
                            setTextBodyUIData(book.getDurChapterPos());
                        }).exceptionally(throwable -> {
                            showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);

                            if (PluginSettingsManager.getInstance().isEnableErrorLog()) {
                                log.error("获取章节列表失败", throwable.getCause());
                            }
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
        initTextBodyUI();

        // 设置正文数据
        setTextBodyUIData(durChapterPos);
    }

    private void initTextBodyUI() {
        // 设置正文面板的字体
        Color fontColor = PluginSettingsManager.getInstance().getTextBodyFontColor();
        textBodyPane.setForeground(new JBColor(fontColor, fontColor));
        textBodyPane.setFont(PluginSettingsManager.getInstance().getTextBodyFont());
        // 设置加载中的提示
        textBodyPane.setText("加载中...");

        if (!textBodyPanel.isShowing()) {
            textBodyPanel.setVisible(true);
            bookshelfPanel.setVisible(false);
        }

        if (!textBodyScrollPane.isShowing()) {
            textBodyScrollPane.setVisible(true);
            textBodyErrorTipsPane.setVisible(false);
        }

        // 获取焦点到文本框
        textBodyPane.requestFocus();
    }

    private void setTextBodyUIData(int durChapterPos) {
        BookDTO book = ReadingSessionManager.getInstance().getCurrentBook();

        // 获取章节标题
        String title = ReadingSessionManager.getInstance().getCurrentChapter().getTitle();

        // 调用API获取正文内容
        CompletableFuture.supplyAsync(() -> ApiUtil.getBookContent(book.getBookUrl(), ReadingSessionManager.getInstance().getCurrentChapterIndex()))
                .thenAccept(bookContent -> {
                    ReadingSessionManager.getInstance().setContent(bookContent);

                    // 设置正文内容
                    textBodyPane.setText(title + "\n" + bookContent);
                    textBodyPane.setCaretPosition(durChapterPos);
                }).exceptionally(throwable -> {
                    showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);

                    if (PluginSettingsManager.getInstance().isEnableErrorLog()) {
                        log.error("获取正文内容失败", throwable.getCause());
                    }

                    return null;
                });

        // 同步阅读进度
        CompletableFuture.runAsync(() -> ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), ReadingSessionManager.getInstance().getCurrentChapterIndex(), title, durChapterPos));
    }

    /**
     * 刷新正文面板（用于章节切换时的 UI 更新）
     * ReaderGlobalFacade 已完成数据操作，此方法只负责 UI 刷新
     */
    public void refreshTextBodyPanel() {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();

        // 1. 获取数据（Facade 已更新）
        String content = sessionManager.getCurrentContent();
        String title = sessionManager.getCurrentChapter().getTitle();

        // 2. 更新 UI（需要在 EDT 线程中执行）
        SwingUtilities.invokeLater(() -> {
            // 初始化 UI
            initTextBodyUI();

            // 设置正文内容
            textBodyPane.setText(title + "\n" + content);
            textBodyPane.setCaretPosition(0);
        });
    }

    private void showErrorTips(JScrollPane textBodyScrollPane, JTextPane textBodyErrorTipsPane) {
        textBodyScrollPane.setVisible(false);
        textBodyErrorTipsPane.setVisible(true);
    }

    private void setAddressUI() {
        List<String> addressHistoryList = AddressHistoryManager.getInstance().getAddressList();
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

    /**
     * 获取单例实例（懒加载）
     */
    public static IndexUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IndexUI();
        }
        return INSTANCE;
    }

    /**
     * 获取书籍
     *
     * @param author 作者
     * @param name   书名
     * @return 书籍信息
     */
    private BookDTO getBook(String author, String name) {
        if (bookshelf == null) {
            return null;
        }
        return bookshelf.get(BOOK_MAP_KEY_FUNC.apply(author, name));
    }

    /**
     * 初始化地址历史记录
     * 在 ToolWindow 首次显示时调用，延迟访问 Service
     */
    public void initAddressHistory() {
        setAddressUI();
    }
}
