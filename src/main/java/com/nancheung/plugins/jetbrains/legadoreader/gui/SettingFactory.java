package com.nancheung.plugins.jetbrains.legadoreader.gui;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.gui.ui.SettingUI;
import com.nancheung.plugins.jetbrains.legadoreader.properties.GlobalSettingProperties;
import com.nancheung.plugins.jetbrains.legadoreader.properties.PropertiesFactory;
import com.nancheung.plugins.jetbrains.legadoreader.toolwindow.IndexUI;
import com.nancheung.plugins.jetbrains.legadoreader.toolwindow.IndexWindowFactory;
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
        SETTING_UI.saveSettings();

        GlobalSettingProperties globalSetting = PropertiesFactory.getGlobalSetting();
        IndexWindowFactory.getInstance().getTextBodyPane().setForeground(globalSetting.getTextBodyFontColor());
        IndexWindowFactory.getInstance().getTextBodyPane().setFont(globalSetting.getTextBodyFont());
    }
    
    public static SettingUI instance() {
        return SETTING_UI;
    }
}
