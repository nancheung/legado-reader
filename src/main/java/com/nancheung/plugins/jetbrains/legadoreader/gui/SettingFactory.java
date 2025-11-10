package com.nancheung.plugins.jetbrains.legadoreader.gui;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import com.nancheung.plugins.jetbrains.legadoreader.gui.ui.SettingUI;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
import com.nancheung.plugins.jetbrains.legadoreader.toolwindow.IndexUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingFactory implements SearchableConfigurable {

    private final static String DISPLAY_NAME = "Legado Reader";

    private final static SettingUI SETTING_UI = new SettingUI();

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
        return SETTING_UI.getComponent();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        // 保存设置
        SETTING_UI.saveSettings();

        // 同步到 Data 静态字段（为了向后兼容）
        PluginSettingsManager settingsManager = PluginSettingsManager.getInstance();
        Data.textBodyFontColor = settingsManager.getTextBodyFontColor();
        Data.textBodyFont = settingsManager.getTextBodyFont();
        Data.apiCustomParam = settingsManager.getApiCustomParam();
        Data.enableErrorLog = settingsManager.isEnableErrorLog();

        // 更新 UI
        IndexUI.getInstance().getTextBodyPane().setForeground(new JBColor(Data.textBodyFontColor, Data.textBodyFontColor));
        IndexUI.getInstance().getTextBodyPane().setFont(Data.textBodyFont);
    }

    public static SettingUI instance() {
        return SETTING_UI;
    }
}
