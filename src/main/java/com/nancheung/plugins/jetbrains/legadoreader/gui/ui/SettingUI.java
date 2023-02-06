package com.nancheung.plugins.jetbrains.legadoreader.gui.ui;

import cn.hutool.core.util.StrUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.ColorPicker;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingUI {
    private JPanel rootPanel;
    
    private JTextField defaultAddress;
    
    private JLabel textBodyFontColorLabel;
    
    public SettingUI() {
        // 读取已有配置
        readSettings();
        // 更新内存数据
        updateMemoryData();
        
        // 正文字体颜色选择的点击事件
        textBodyFontColorLabel.addMouseListener(chooseColorMouseListener());
    }
    
    @NotNull
    private MouseAdapter chooseColorMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Color newColor = ColorPicker.showDialog(rootPanel, textBodyFontColorLabel.getText() + " Color", textBodyFontColorLabel.getForeground(), true, null, true);
                if (newColor != null) {
                    textBodyFontColorLabel.setForeground(newColor);
                }
            }
        };
    }
    
    public JComponent getComponent() {
        return rootPanel;
    }
    
    public void readSettings() {
        // 读取本地配置
        String address = PropertiesComponent.getInstance().getValue(Constant.PLUGIN__SETTING_PREFIX + ".address");
        String textBodyFontColor = PropertiesComponent.getInstance().getValue(Constant.PLUGIN__SETTING_PREFIX + ".textBodyFontColor");
        
        if (StrUtil.isNotBlank(address)) {
            defaultAddress.setText(address);
        }
        
        if (StrUtil.isNotBlank(textBodyFontColor)) {
            assert textBodyFontColor != null;
            int rgb = Integer.parseInt(textBodyFontColor);
            textBodyFontColorLabel.setForeground(new Color(rgb));
        }
    }
    
    
    public void saveSettings() {
        // 持久化本地配置
        PropertiesComponent.getInstance().setValue(Constant.PLUGIN__SETTING_PREFIX + ".address", defaultAddress.getText());
        PropertiesComponent.getInstance().setValue(Constant.PLUGIN__SETTING_PREFIX + ".textBodyFontColor", String.valueOf(textBodyFontColorLabel.getForeground().getRGB()));
        
        // 更新内存数据
        updateMemoryData();
    }
    
    private void updateMemoryData() {
        Data.address = defaultAddress.getText();
        Data.textBodyFontColor = textBodyFontColorLabel.getForeground();
    }
    
    
}
