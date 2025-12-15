package com.nancheung.plugins.jetbrains.legadoreader.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReadingEvent;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReadingEventListener;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderGlobalFacade;
import com.nancheung.plugins.jetbrains.legadoreader.gui.SettingFactory;
import com.nancheung.plugins.jetbrains.legadoreader.storage.AddressHistoryStorage;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@Getter
public class IndexUI {


    /**
     * UI 状态枚举
     */
    private enum UIState {
        /**
         * 初始化状态
         */
        INITIALIZED,

        /**
         * 加载中状态
         */
        LOADING,

        /**
         * 加载成功状态
         */
        SUCCESS,

        /**
         * 加载失败状态
         */
        FAILED
    }

    /**
     * 当前 UI 状态
     */
    private UIState currentState = UIState.INITIALIZED;


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

        // 订阅阅读事件
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect()
                .subscribe(ReadingEventListener.TOPIC, (ReadingEventListener) event -> INSTANCE.onReadingEvent(event));

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

                    if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
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

            AddressHistoryStorage.getInstance().addAddress(addressTextField.getText());

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

                // 获取当前点击的书籍信息
                TableModel model = ((JTable) evt.getSource()).getModel();
                String name = model.getValueAt(row, 0).toString();
                String author = model.getValueAt(row, 3).toString();

                // 获取书籍信息
                BookDTO book = getBook(author, name);

                // 使用 Facade 加载章节（事件驱动）
                ReaderGlobalFacade.getInstance().loadChapter(book, book.getDurChapterIndex());
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

    private void showErrorTips(JScrollPane textBodyScrollPane, JTextPane textBodyErrorTipsPane) {
        textBodyScrollPane.setVisible(false);
        textBodyErrorTipsPane.setVisible(true);
    }

    private void setAddressUI() {
        List<String> addressHistoryList = AddressHistoryStorage.getInstance().getAddressList();
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

    // ==================== 事件驱动的阅读功能 ====================

    /**
     * 阅读事件分发器
     * 根据事件状态调用不同的处理方法
     *
     * @param event 阅读事件
     */
    public void onReadingEvent(ReadingEvent event) {
        // 确保在 EDT 线程中执行 UI 更新
        ApplicationManager.getApplication().invokeLater(() -> {
            switch (event.status()) {
                case LOADING_STARTED -> handleLoadingStarted(event);
                case LOADING_SUCCESS -> handleLoadingSuccess(event);
                case LOADING_FAILED -> handleLoadingFailed(event);
            }
        });
    }

    /**
     * 处理"开始加载"事件
     * UI 进入加载中状态，显示加载提示
     *
     * @param event 阅读事件
     */
    private void handleLoadingStarted(ReadingEvent event) {
        currentState = UIState.LOADING;

        log.info("UI 进入加载状态: book={}, chapterIndex={}",
                event.book().getName(), event.chapter().getIndex());

        // 显示正文面板
        if (!textBodyPanel.isShowing()) {
            textBodyPanel.setVisible(true);
            bookshelfPanel.setVisible(false);
        }

        // 隐藏错误提示
        if (!textBodyScrollPane.isShowing()) {
            textBodyScrollPane.setVisible(true);
            textBodyErrorTipsPane.setVisible(false);
        }

        // 设置加载提示
        textBodyPane.setText("加载中...");

        // 获取焦点
        textBodyPane.requestFocus();
    }

    /**
     * 处理"加载成功"事件
     * UI 进入成功状态，显示章节内容
     *
     * @param event 阅读事件
     */
    private void handleLoadingSuccess(ReadingEvent event) {
        currentState = UIState.SUCCESS;

        log.info("UI 加载成功: book={}, chapter={}",
                event.book().getName(), event.chapter().getTitle());

        // 设置正文字体样式
        Color fontColor = PluginSettingsStorage.getInstance().getTextBodyFontColor();
        textBodyPane.setForeground(new JBColor(fontColor, fontColor));
        textBodyPane.setFont(PluginSettingsStorage.getInstance().getTextBodyFont());

        // 设置正文内容
        String title = event.chapter().getTitle();
        String content = event.content();
        textBodyPane.setText(title + "\n" + content);

        // 设置光标位置
        textBodyPane.setCaretPosition(event.chapterPosition());
    }

    /**
     * 处理"加载失败"事件
     * UI 进入失败状态，显示错误提示
     *
     * @param event 阅读事件
     */
    private void handleLoadingFailed(ReadingEvent event) {
        currentState = UIState.FAILED;

        log.warn("UI 加载失败: book={}, chapterIndex={}, error={}",
                event.book().getName(),
                event.chapter().getIndex(),
                event.error() != null ? event.error().getMessage() : "Unknown");

        // 显示错误提示
        showErrorTips(textBodyScrollPane, textBodyErrorTipsPane);

        // 记录详细错误日志
        if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
            log.error("章节加载失败", event.error());
        }
    }
}
