package com.nancheung.plugins.jetbrains.legadoreader.gui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import com.nancheung.plugins.jetbrains.legadoreader.gui.ui.IndexUI;
import com.nancheung.plugins.jetbrains.legadoreader.gui.ui.SettingUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingFactory implements SearchableConfigurable {
    
    private final static String DISPLAY_NAME = "Legado Reader";
    
    private final static SettingUI SETTING_UI = new SettingUI();
    
    @Override
    public @NotNull String getId() {
        return Constant.PLUGIN__SETTING_ID;
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
    public void apply() throws ConfigurationException {
        SETTING_UI.saveSettings();
        IndexUI instance = IndexWindowFactory.instance();
    
        instance.setAddress(Data.address);
        instance.setTextBodyFontColor(Data.textBodyFontColor);
        instance.setTextBodyFontSize(Data.textBodyFontSize);
    }
    
    public static SettingUI instance() {
        return SETTING_UI;
    }
}
