package com.nancheung.plugins.jetbrains.legadoreader.gui;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.gui.ui.SettingUI;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import com.nancheung.plugins.jetbrains.legadoreader.toolwindow.IndexUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class SettingFactory implements SearchableConfigurable {

    private final static String DISPLAY_NAME = "Legado Reader";

    /**
     * SettingUI 实例（懒加载，避免在类加载时访问服务）
     */
    private static SettingUI settingUI;

    @Override
    public @NotNull String getId() {
        return Constant.PLUGIN_SETTING_ID;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return SettingFactory.DISPLAY_NAME;
    }

    @Override
    public @Nullable JComponent createComponent() {
        SettingUI ui = instance();
        // 每次打开设置页时，从存储中重新读取设置到 UI（解决取消后再次打开显示未保存值的问题）
        ui.readSettings(PluginSettingsStorage.getInstance().getState());
        return ui.getComponent();
    }

    @Override
    public boolean isModified() {
        if (settingUI == null) {
            return false;
        }

        PluginSettingsStorage.State state = PluginSettingsStorage.getInstance().getState();
        if (state == null) {
            return false;
        }

        SettingUI ui = instance();

        // 比较 UI 当前值与存储值是否不同
        boolean fontColorModified = state.textBodyFontColorRgb == null ||
                state.textBodyFontColorRgb != ui.getTextBodyFontColorLabel().getForeground().getRGB();

        boolean fontSizeModified = state.textBodyFontSize == null ||
                !state.textBodyFontSize.equals(ui.getTextBodyFontSizeSpinner().getValue());

        String currentCustomParam = ui.getApiCustomParamTextArea().getText();
        String storedCustomParam = state.apiCustomParam == null ? "" : state.apiCustomParam;
        boolean customParamModified = !currentCustomParam.equals(storedCustomParam);

        boolean errorLogModified = Boolean.TRUE.equals(state.enableErrorLog) != ui.getEnableErrorLogCheckBox().isSelected();

        boolean inLineModelModified = Boolean.TRUE.equals(state.enableShowBodyInLine) != ui.getEnableInLineModelCheckBox().isSelected();

        // 新增：字体名称比较
        String currentFontFamily = (String) ui.getTextBodyFontFamilyComboBox().getSelectedItem();
        String storedFontFamily = state.textBodyFontFamily;
        if (storedFontFamily == null || storedFontFamily.isEmpty()) {
            try {
                storedFontFamily = com.intellij.openapi.editor.colors.EditorColorsManager.getInstance()
                        .getGlobalScheme()
                        .getFont(com.intellij.openapi.editor.colors.EditorFontType.PLAIN)
                        .getFamily();
            } catch (Exception e) {
                storedFontFamily = new JLabel().getFont().getFamily();
            }
        }
        boolean fontFamilyModified = !currentFontFamily.equals(storedFontFamily);

        // 新增：行高比较（注意精度问题）
        double currentLineHeight = (double) ui.getTextBodyLineHeightSpinner().getValue();
        double storedLineHeight = state.textBodyLineHeight != null ? state.textBodyLineHeight : 1.5;
        boolean lineHeightModified = Math.abs(currentLineHeight - storedLineHeight) > 0.001;

        return fontColorModified || fontSizeModified || customParamModified ||
                errorLogModified || inLineModelModified || fontFamilyModified || lineHeightModified;
    }

    @Override
    public void reset() {
        // 重置为上次保存的值
        if (settingUI != null) {
            settingUI.readSettings(PluginSettingsStorage.getInstance().getState());
        }
    }

    @Override
    public void apply() {
        // 保存设置
        saveSettings();

        // 更新 UI
        PluginSettingsStorage storage = PluginSettingsStorage.getInstance();
        java.awt.Color fontColor = storage.getTextBodyFontColor();
        Font font = storage.getTextBodyFont(); // 已支持自定义字体
        double lineHeight = storage.getTextBodyLineHeight();

        JTextPane textBodyPane = IndexUI.getInstance().getTextBodyPane();
        textBodyPane.setForeground(new JBColor(fontColor, fontColor));
        textBodyPane.setFont(font);

        // 新增：应用行高
        applyLineHeightToTextPane(textBodyPane, lineHeight);
    }

    private void applyLineHeightToTextPane(JTextPane textPane, double lineHeight) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        float lineSpacing = (float) (lineHeight - 1.0);
        StyleConstants.setLineSpacing(attrs, lineSpacing);

        StyledDocument doc = textPane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
    }

    /**
     * 获取 SettingUI 实例（懒加载）
     * 只在用户打开设置页面时才创建实例，避免在类加载时访问服务
     *
     * @return SettingUI 实例
     */
    public static SettingUI instance() {
        if (settingUI == null) {
            settingUI = new SettingUI();

            // 读取已有配置
            settingUI.readSettings(PluginSettingsStorage.getInstance().getState());
        }
        return settingUI;
    }

    public void saveSettings() {
        SettingUI ui = instance();
        // 直接修改 State 字段，框架会自动持久化
        PluginSettingsStorage.State state = PluginSettingsStorage.getInstance().getState();
        if (state == null) {
            return;
        }

        state.textBodyFontColorRgb = ui.getTextBodyFontColorLabel().getForeground().getRGB();
        state.textBodyFontSize = (int) ui.getTextBodyFontSizeSpinner().getValue();
        state.apiCustomParam = ui.getApiCustomParamTextArea().getText();
        state.enableErrorLog = ui.getEnableErrorLogCheckBox().isSelected();
        state.enableShowBodyInLine = ui.getEnableInLineModelCheckBox().isSelected();

        // 新增：保存字体名称和行高
        state.textBodyFontFamily = (String) ui.getTextBodyFontFamilyComboBox().getSelectedItem();
        state.textBodyLineHeight = (double) ui.getTextBodyLineHeightSpinner().getValue();
    }
}
