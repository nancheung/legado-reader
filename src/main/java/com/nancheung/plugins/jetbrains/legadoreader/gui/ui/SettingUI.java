package com.nancheung.plugins.jetbrains.legadoreader.gui.ui;

import cn.hutool.core.util.StrUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.ColorPicker;
import com.nancheung.plugins.jetbrains.legadoreader.common.ConstantEnum;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingUI {
    private JPanel rootPanel;
    
    private JPanel conventionalPanel;
    private JTextField defaultAddress;
    
    private JPanel readUiPanel;
    private JLabel chooseColorLabel;
    
    public SettingUI() {
        readSettings();
        saveSettings();
        
        chooseColorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Color newColor = ColorPicker.showDialog(rootPanel, chooseColorLabel.getText() + " Color", chooseColorLabel.getForeground(), true, null, true);
                if (newColor != null) {
                    chooseColorLabel.setForeground(newColor);
                }
            }
        });
    }
    
    public JComponent getComponent() {
        return rootPanel;
    }
    
    public void readSettings() {
        String address = PropertiesComponent.getInstance().getValue(ConstantEnum.PLUGIN__SETTING_PREFIX.getValue() + ".address");
        String textBodyFontColor = PropertiesComponent.getInstance().getValue(ConstantEnum.PLUGIN__SETTING_PREFIX.getValue() + ".textBodyFontColor");
        
        if (StrUtil.isNotBlank(address)) {
            defaultAddress.setText(address);
        }
        
        if (StrUtil.isNotBlank(textBodyFontColor)) {
            assert textBodyFontColor != null;
            int rgb = Integer.parseInt(textBodyFontColor);
            chooseColorLabel.setForeground(new Color(rgb));
        }
    }
    
    
    public void saveSettings() {
        PropertiesComponent.getInstance().setValue(ConstantEnum.PLUGIN__SETTING_PREFIX.getValue() + ".address", defaultAddress.getText());
        PropertiesComponent.getInstance().setValue(ConstantEnum.PLUGIN__SETTING_PREFIX.getValue() + ".textBodyFontColor", String.valueOf(chooseColorLabel.getForeground().getRGB()));
        
        Data.address = defaultAddress.getText();
        Data.textBodyFontColor = chooseColorLabel.getForeground();
    }
    
    
}
