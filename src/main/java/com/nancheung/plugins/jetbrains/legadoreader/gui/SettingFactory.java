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

        return fontColorModified || fontSizeModified || customParamModified || errorLogModified || inLineModelModified;
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
        IndexUI.getInstance().getTextBodyPane().setForeground(new JBColor(fontColor, fontColor));
        IndexUI.getInstance().getTextBodyPane().setFont(storage.getTextBodyFont());
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
    }
}
