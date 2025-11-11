package com.nancheung.plugins.jetbrains.legadoreader.gui;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.gui.ui.SettingUI;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
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
        return instance().getComponent();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        // 保存设置
        instance().saveSettings();

        // 更新 UI
        PluginSettingsManager settingsManager = PluginSettingsManager.getInstance();
        java.awt.Color fontColor = settingsManager.getTextBodyFontColor();
        IndexUI.getInstance().getTextBodyPane().setForeground(new JBColor(fontColor, fontColor));
        IndexUI.getInstance().getTextBodyPane().setFont(settingsManager.getTextBodyFont());
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
        }
        return settingUI;
    }
}
