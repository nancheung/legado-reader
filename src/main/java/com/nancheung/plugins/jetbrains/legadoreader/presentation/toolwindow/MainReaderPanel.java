package com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBPanel;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.SelectBookPayload;
import com.nancheung.plugins.jetbrains.legadoreader.event.PaginationEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.ProgressSyncEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.ReadingEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.SettingsChangedEvent;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.common.UIEventSubscriber;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.styling.TextBodyStyling;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.panel.TextBodyPanel;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.panel.BookshelfPanel;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.handler.MainPanelEventHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
@Getter
public class MainReaderPanel extends UIEventSubscriber {

    // ==================== 卡片常量 ====================
    private static final String CARD_BOOKSHELF = "BOOKSHELF";
    private static final String CARD_TEXT_BODY = "TEXT_BODY";

    // ==================== 根面板 ====================
    private JBPanel<?> rootPanel;
    private CardLayout mainCardLayout;

    // ==================== 子面板组件 ====================
    private BookshelfPanel bookshelfPanel;
    private TextBodyPanel textBodyPanel;

    // ==================== 事件处理器 ====================
    private final MainPanelEventHandler eventHandler;

    // ==================== 单例实例 ====================
    private static MainReaderPanel INSTANCE;

    // ==================== 样式管理器 ====================
    private final TextBodyStyling textBodyStyling = new TextBodyStyling();

    // ==================== 构造函数 ====================
    public MainReaderPanel() {
        super();

        // 创建 UI 组件
        createRootPanel();

        // 创建事件处理器
        this.eventHandler = new MainPanelEventHandler(this, bookshelfPanel, textBodyPanel);

        // 初始加载书架
        initialLoadBookshelf();
    }

    // ==================== 组件创建方法 ====================

    /**
     * 创建根面板
     */
    private void createRootPanel() {
        mainCardLayout = new CardLayout();
        rootPanel = new JBPanel<>(mainCardLayout);

        // 创建书架面板（传入书籍选择回调）
        bookshelfPanel = new BookshelfPanel();
        rootPanel.add(bookshelfPanel, CARD_BOOKSHELF);

        // 创建正文面板
        textBodyPanel = new TextBodyPanel();
        rootPanel.add(textBodyPanel, CARD_TEXT_BODY);

        // 默认显示书架
        mainCardLayout.show(rootPanel, CARD_BOOKSHELF);
    }


    // ==================== 初始化方法 ====================


    /**
     * 初始加载书架
     */
    private void initialLoadBookshelf() {
        bookshelfPanel.refreshBookshelf();
    }

    // ==================== 面板切换方法 ====================

    /**
     * 显示书架面板
     */
    public void showBookshelfPanel() {
        mainCardLayout.show(rootPanel, CARD_BOOKSHELF);
    }

    /**
     * 显示正文面板
     */
    public void showTextBodyPanel() {
        mainCardLayout.show(rootPanel, CARD_TEXT_BODY);
    }

    // ==================== 公共接口方法 ====================

    /**
     * 获取根组件
     */
    public JComponent getComponent() {
        return rootPanel;
    }

    /**
     * 获取单例实例
     */
    public static MainReaderPanel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainReaderPanel();
        }
        return INSTANCE;
    }

    /**
     * 初始化地址历史记录
     * 在 ToolWindow 首次显示时调用
     */
    public void initAddressHistory() {
        bookshelfPanel.initAddressHistory();
    }

    // ==================== 事件处理方法 ====================

    /**
     * 重写父类方法：处理阅读事件
     */
    @Override
    protected void onReadingEvent(ReadingEvent event) {
        eventHandler.handleReadingEvent(event);
    }

    /**
     * 重写父类方法：处理分页事件
     */
    @Override
    protected void onPaginationEvent(PaginationEvent event) {
        eventHandler.handlePaginationEvent(event);
    }

    /**
     * 重写父类方法：处理设置变更事件
     */
    @Override
    protected void onSettingsChangedEvent(SettingsChangedEvent event) {
        eventHandler.handleSettingsChangedEvent(event);
    }

    /**
     * 重写父类方法：处理进度同步事件
     */
    @Override
    protected void onProgressSyncEvent(ProgressSyncEvent event) {
        eventHandler.handleProgressSyncEvent(event);
    }
}
