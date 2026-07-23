package com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.panel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import com.nancheung.plugins.jetbrains.legadoreader.presentation.toolwindow.styling.TextBodyStyling;
import com.nancheung.plugins.jetbrains.legadoreader.service.ProgressSyncService;

import javax.swing.*;
import java.awt.*;

/**
 * 正文面板组件
 * 负责显示章节内容、工具栏和错误提示
 *
 * @author NanCheung
 */
public class TextBodyPanel extends JBPanel<TextBodyPanel> {

    // ==================== 卡片常量 ====================
    private static final String CARD_CONTENT = "CONTENT";
    private static final String CARD_ERROR = "ERROR";

    // ==================== 错误提示文本 ====================
    private static final String ERROR_MESSAGE = """
            请求内容失败，请检查web服务是否开启、url是否正确、网络是否正常？

            小提示：可以在File -> Settings -> Tools -> Legado Reader中进行更多设置哦~
            """;

    // ==================== UI 组件 ====================
    private final ActionToolbar actionToolbar;
    private final JTextPane textBodyPane;
    private final JBPanel<?> textBodyContentPanel;
    private final CardLayout textBodyContentLayout;
    private final JBLabel syncStatusLabel;

    /**
     * 同步状态提示自动隐藏计时器
     */
    private Timer syncStatusHideTimer;

    // ==================== 样式管理器 ====================
    private final TextBodyStyling textBodyStyling;

    // ==================== 构造函数 ====================
    public TextBodyPanel() {
        super(new BorderLayout());
        this.textBodyStyling = new TextBodyStyling();

        // 1. 顶部工具栏
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction(Constant.PLUGIN_TOOL_BAR_ID);
        this.actionToolbar = actionManager.createActionToolbar(Constant.PLUGIN_TOOL_BAR_ID, actionGroup, true);
        this.actionToolbar.setTargetComponent(this);

        JComponent toolbarComponent = actionToolbar.getComponent();
        toolbarComponent.setBorder(JBUI.Borders.empty());

        // 顶部：工具栏 + 右侧同步状态提示
        syncStatusLabel = new JBLabel();
        syncStatusLabel.setForeground(JBColor.GRAY);
        syncStatusLabel.setBorder(JBUI.Borders.emptyRight(8));
        syncStatusLabel.setVisible(false);

        JBPanel<?> topPanel = new JBPanel<>(new BorderLayout());
        topPanel.add(toolbarComponent, BorderLayout.CENTER);
        topPanel.add(syncStatusLabel, BorderLayout.EAST);
        this.add(topPanel, BorderLayout.NORTH);

        // 2. 中央内容区（使用 CardLayout）
        textBodyContentLayout = new CardLayout();
        textBodyContentPanel = new JBPanel<>(textBodyContentLayout);

        // 2.1 内容卡片：正文
        textBodyPane = new JTextPane();
        textBodyPane.setEditable(false);
        JBScrollPane scrollPane = new JBScrollPane(textBodyPane);
        textBodyContentPanel.add(scrollPane, CARD_CONTENT);

        // 滚动阅读时防抖同步章节内位置（首个可见字符的偏移）
        scrollPane.getViewport().addChangeListener(e -> syncScrollProgress(scrollPane.getViewport()));

        // 2.2 错误卡片（右上角提供退出阅读按钮，加载失败时仍可返回书架）
        textBodyContentPanel.add(createErrorPanel(), CARD_ERROR);

        this.add(textBodyContentPanel, BorderLayout.CENTER);

        // 默认显示内容
        showContent();
    }

    // ==================== UI 创建辅助方法 ====================

    /**
     * 创建错误标签
     */
    private JBLabel createErrorLabel() {
        JBLabel label = new JBLabel();
        label.setText("<html><center>" + ERROR_MESSAGE.replace("\n", "<br>") + "</center></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(JBColor.GRAY);
        return label;
    }

    /**
     * 将组件包装在居中面板中
     */
    private JBPanel<?> wrapCentered(JComponent component) {
        JBPanel<?> wrapper = new JBPanel<>(new GridBagLayout());
        wrapper.add(component);
        return wrapper;
    }

    /**
     * 创建错误卡片面板
     * 右上角提供"退出阅读"按钮，中央为错误提示
     */
    private JBPanel<?> createErrorPanel() {
        JBPanel<?> errorPanel = new JBPanel<>(new BorderLayout());

        JButton exitButton = new JButton("退出阅读");
        exitButton.addActionListener(e ->
                CommandBus.getInstance().dispatchAsync(Command.of(CommandType.BACK_TO_BOOKSHELF)));

        JBPanel<?> buttonBar = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        buttonBar.add(exitButton);

        errorPanel.add(buttonBar, BorderLayout.NORTH);
        errorPanel.add(wrapCentered(createErrorLabel()), BorderLayout.CENTER);
        return errorPanel;
    }

    // ==================== 状态切换方法 ====================

    /**
     * 显示正文内容（隐藏错误）
     */
    public void showContent() {
        textBodyContentLayout.show(textBodyContentPanel, CARD_CONTENT);
    }

    /**
     * 显示错误提示（隐藏内容）
     */
    public void showError() {
        textBodyContentLayout.show(textBodyContentPanel, CARD_ERROR);
    }

    // ==================== 内容操作方法 ====================

    /**
     * 设置正文文本
     *
     * @param text 文本内容
     */
    public void setText(String text) {
        textBodyPane.setText(text);
    }

    /**
     * 获取正文文本
     *
     * @return 文本内容
     */
    public String getText() {
        return textBodyPane.getText();
    }

    /**
     * 设置光标位置
     *
     * @param position 光标位置
     */
    public void setCaretPosition(int position) {
        textBodyPane.setCaretPosition(position);
    }

    /**
     * 滚动到指定位置
     *
     * @param position 目标位置
     */
    public void scrollToPosition(int position) {
        try {
            Rectangle viewRect = textBodyPane.modelToView2D(position).getBounds();
            textBodyPane.scrollRectToVisible(viewRect);
        } catch (javax.swing.text.BadLocationException e) {
            // 忽略无效位置
        }
    }

    /**
     * 请求焦点
     */
    public void requestTextFocus() {
        textBodyPane.requestFocus();
    }

    /**
     * 显示同步状态提示（顶部右侧）
     *
     * @param text       提示文本
     * @param autoHideMs 自动隐藏延迟（毫秒），小于等于 0 表示不自动隐藏
     */
    public void showSyncStatus(String text, int autoHideMs) {
        if (syncStatusHideTimer != null) {
            syncStatusHideTimer.stop();
            syncStatusHideTimer = null;
        }

        syncStatusLabel.setText(text);
        syncStatusLabel.setVisible(true);

        if (autoHideMs > 0) {
            syncStatusHideTimer = new Timer(autoHideMs, e -> syncStatusLabel.setVisible(false));
            syncStatusHideTimer.setRepeats(false);
            syncStatusHideTimer.start();
        }
    }

    /**
     * 滚动时防抖同步阅读进度
     * 把视口首个可见字符的文档偏移换算为章节内位置（扣除标题行），交给同步服务防抖处理
     *
     * @param viewport 正文滚动视口
     */
    private void syncScrollProgress(JViewport viewport) {
        ReadingSession session = ReadingSessionManager.getInstance().getSession();
        if (session == null || session.currentContent() == null) {
            return;
        }

        int docOffset = textBodyPane.viewToModel2D(new Point(0, viewport.getViewPosition().y));
        if (docOffset < 0) {
            return;
        }

        // 正文文档 = 标题 + "\n" + 章节内容，换算回章节内偏移
        BookChapterDTO chapter = session.getCurrentChapter();
        String title = chapter != null ? chapter.getTitle() : null;
        int titleLength = (title != null && !title.isEmpty()) ? title.length() + 1 : 0;
        int position = Math.max(0, docOffset - titleLength);

        ProgressSyncService.getInstance().scheduleSyncCurrentPosition(position);
    }

    // ==================== 样式操作方法 ====================

    /**
     * 应用样式
     *
     * @param fontColor  字体颜色
     * @param font       字体
     * @param lineHeight 行高
     */
    public void applyStyle(JBColor fontColor, Font font, double lineHeight) {
        textBodyStyling.apply(textBodyPane, fontColor, font, lineHeight);
    }

    /**
     * 从设置中应用样式
     */
    public void applyStyleFromSettings() {
        textBodyStyling.applyFromSettings(textBodyPane);
    }

    // ==================== 查询方法 ====================

    /**
     * 检查内容是否可见
     *
     * @return true 如果内容可见
     */
    public boolean isContentVisible() {
        return this.isVisible();
    }

    /**
     * 获取 ActionToolbar
     *
     * @return ActionToolbar 实例
     */
    public ActionToolbar getActionToolbar() {
        return actionToolbar;
    }
}
