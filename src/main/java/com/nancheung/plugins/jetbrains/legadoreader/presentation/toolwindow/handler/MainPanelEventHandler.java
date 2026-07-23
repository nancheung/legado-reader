package com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.handler;

import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.event.PaginationEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.ProgressSyncEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.ReadingEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.SettingsChangedEvent;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.MainReaderPanel;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.panel.BookshelfPanel;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.panel.TextBodyPanel;
import com.nancheung.plugins.jetbrains.legadoreader.service.IPaginationManager;
import com.nancheung.plugins.jetbrains.legadoreader.service.PaginationManager;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

/**
 * 主面板事件处理器
 * 负责处理所有 UI 事件的业务逻辑
 *
 * @author NanCheung
 */
@Slf4j
public class MainPanelEventHandler {

    // ==================== UI 状态枚举 ====================
    private enum UIState {
        INITIALIZED,
        LOADING,
        SUCCESS,
        FAILED
    }

    private UIState currentState = UIState.INITIALIZED;

    // ==================== 面板引用 ====================
    private final MainReaderPanel mainPanel;
    private final BookshelfPanel bookshelfPanel;
    private final TextBodyPanel textBodyPanel;

    // ==================== 构造函数 ====================
    public MainPanelEventHandler(
            MainReaderPanel mainPanel,
            BookshelfPanel bookshelfPanel,
            TextBodyPanel textBodyPanel) {
        this.mainPanel = mainPanel;
        this.bookshelfPanel = bookshelfPanel;
        this.textBodyPanel = textBodyPanel;
    }

    // ==================== 事件处理方法 ====================

    /**
     * 处理阅读事件
     */
    public void handleReadingEvent(ReadingEvent event) {
        switch (event.type()) {
            case CHAPTER_LOADING -> handleLoadingStarted(event);
            case CHAPTER_LOADED -> handleLoadingSuccess(event);
            case CHAPTER_LOAD_FAILED -> handleLoadingFailed(event);
            case SESSION_ENDED -> handleSessionEnded();
        }
    }

    /**
     * 处理分页事件
     */
    public void handlePaginationEvent(PaginationEvent event) {
        // 只处理页码变更事件（PAGE_CHANGED）
        if (event.type() != PaginationEvent.PaginationEventType.PAGE_CHANGED) {
            return;
        }

        // 如果正文面板不可见，跳过（用户可能在书架）
        if (!textBodyPanel.isContentVisible()) {
            log.debug("正文面板不可见，跳过光标同步");
            return;
        }

        // 获取当前页数据
        PaginationManager paginationManager = PaginationManager.getInstance();
        IPaginationManager.PageData currentPage = paginationManager.getCurrentPage();

        if (currentPage == null || currentPage.startPos() < 0) {
            log.warn("分页事件但无当前页数据或起始位置无效");
            return;
        }

        // 获取当前阅读会话以获取标题
        ReadingSession session = ReadingSessionManager.getInstance().getSession();
        if (session == null || session.currentContent() == null) {
            log.debug("无当前阅读会话，跳过光标同步");
            return;
        }

        // 计算标题长度（标题 + 换行符）
        String title = session.chapters().get(session.currentChapterIndex()).getTitle();
        int titleLength = (title != null && !title.isEmpty()) ? title.length() + 1 : 0;

        // 计算光标位置
        int caretPosition = titleLength + currentPage.startPos();

        // 限制在有效范围内
        String currentText = textBodyPanel.getText();
        caretPosition = Math.min(caretPosition, currentText.length());

        // 设置光标位置并滚动
        textBodyPanel.setCaretPosition(caretPosition);
        textBodyPanel.scrollToPosition(caretPosition);

        log.debug("光标同步完成：页码 {}/{}, 光标位置 {}",
                event.currentPage(), event.totalPages(), caretPosition);
    }

    /**
     * 处理进度同步事件
     * 在正文面板顶部右侧展示同步状态提示
     */
    public void handleProgressSyncEvent(ProgressSyncEvent event) {
        // 正文面板不可见时跳过（用户可能已回到书架）
        if (!textBodyPanel.isContentVisible()) {
            return;
        }

        switch (event.status()) {
            case SYNCING -> textBodyPanel.showSyncStatus("进度同步中…", 0);
            case SYNCED -> textBodyPanel.showSyncStatus("进度已同步", 3000);
            case SYNC_FAILED -> textBodyPanel.showSyncStatus("进度同步失败", 5000);
        }
    }

    /**
     * 处理设置变更事件
     * 当用户在设置页面保存字体设置后，立即刷新 UI
     */
    public void handleSettingsChangedEvent(SettingsChangedEvent event) {
        // 只处理字体设置变更
        if (event.type() != SettingsChangedEvent.SettingsChangedType.FONT_SETTINGS
                && event.type() != SettingsChangedEvent.SettingsChangedType.ALL_SETTINGS) {
            return;
        }

        log.info("收到设置变更事件，刷新正文面板字体样式");

        // 只有在正文面板可见且有内容时才更新
        if (!textBodyPanel.isContentVisible()) {
            log.debug("正文面板不可见，跳过字体刷新");
            return;
        }

        ReadingSession session = ReadingSessionManager.getInstance().getSession();
        if (session == null || session.currentContent() == null) {
            log.debug("无当前阅读会话，跳过字体刷新");
            return;
        }

        // 从事件中获取新设置（优先使用事件中的值，回退到存储）
        PluginSettingsStorage.State state = PluginSettingsStorage.getInstance().getState();
        JBColor fontColor = event.fontColor() != null ? event.fontColor() : state.textBodyFontColor;
        Font font = event.font() != null ? event.font() : state.textBodyFont;
        double lineHeight = event.lineHeight() != null ? event.lineHeight() : state.textBodyLineHeight;

        // 更新正文面板的字体样式
        textBodyPanel.applyStyle(fontColor, font, lineHeight);

        log.debug("字体样式已更新：color={}, font={}, lineHeight={}",
                fontColor, font.getFamily() + "/" + font.getSize(), lineHeight);
    }

    // ==================== 私有处理方法 ====================

    /**
     * 处理"开始加载"事件
     */
    private void handleLoadingStarted(ReadingEvent event) {
        currentState = UIState.LOADING;

        log.info("UI 进入加载状态: book={}, chapterIndex={}",
                event.book().getName(), event.chapter().getIndex());

        // 切换到正文面板并显示内容区
        mainPanel.showTextBodyPanel();
        textBodyPanel.showContent();

        // 设置加载提示
        textBodyPanel.setText("加载中...");

        // 获取焦点
        textBodyPanel.requestTextFocus();
    }

    /**
     * 处理"加载成功"事件
     */
    private void handleLoadingSuccess(ReadingEvent event) {
        currentState = UIState.SUCCESS;

        log.info("UI 加载成功: book={}, chapter={}",
                event.book().getName(), event.chapter().getTitle());

        // 设置正文内容
        String title = event.chapter().getTitle();
        String content = event.content();
        textBodyPanel.setText(title + "\n" + content);

        // 应用样式（在 setText 之后）
        textBodyPanel.applyStyleFromSettings();

        // 设置光标位置
        textBodyPanel.setCaretPosition(event.chapterPosition());
    }

    /**
     * 处理"加载失败"事件
     */
    private void handleLoadingFailed(ReadingEvent event) {
        currentState = UIState.FAILED;

        log.warn("UI 加载失败: book={}, chapterIndex={}, error={}",
                event.book().getName(),
                event.chapter().getIndex(),
                event.error() != null ? event.error().getMessage() : "Unknown");

        // 显示错误提示
        textBodyPanel.showError();

        // 记录详细错误日志
        if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
            log.error("章节加载失败", event.error());
        }
    }

    /**
     * 处理"会话结束"事件
     */
    private void handleSessionEnded() {
        currentState = UIState.INITIALIZED;
        log.info("会话结束，返回书架");

        // 刷新并显示书架面板
        bookshelfPanel.refreshBookshelf();
        mainPanel.showBookshelfPanel();

        // 清空正文内容
        textBodyPanel.setText("");
    }
}
