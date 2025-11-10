package com.nancheung.plugins.jetbrains.legadoreader.gui.ui;

import com.intellij.ui.ColorPicker;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.PluginSettingsData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingUI {
    private JPanel rootPanel;

    private JLabel textBodyFontColorLabel;

    private JSpinner textBodyFontSizeSpinner;
    private JTextArea apiCustomParamTextArea;
    private JCheckBox enableErrorLogCheckBox;
    private JCheckBox enableInLineModelCheckBox;

    private final PluginSettingsManager settingsManager = PluginSettingsManager.getInstance();

    public SettingUI() {
        // 正文大小输入范围
        textBodyFontSizeSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
        // 读取已有配置
        readSettings();

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
        // 从 SettingsManager 读取配置
        PluginSettingsData data = settingsManager.getSettings();

        if (data.textBodyFontColorRgb() != null) {
            textBodyFontColorLabel.setForeground(new JBColor(new Color(data.textBodyFontColorRgb()), new Color(data.textBodyFontColorRgb())));
        }

        if (data.textBodyFontSize() != null && data.textBodyFontSize() > 0) {
            textBodyFontSizeSpinner.setValue(data.textBodyFontSize());
        }

        if (data.apiCustomParam() != null && !data.apiCustomParam().isEmpty()) {
            apiCustomParamTextArea.setText(data.apiCustomParam());
        }

        enableErrorLogCheckBox.setSelected(Boolean.TRUE.equals(data.enableErrorLog()));
        enableInLineModelCheckBox.setSelected(Boolean.TRUE.equals(data.enableShowBodyInLine()));
    }


    public void saveSettings() {
        // 从 UI 组件构建设置数据
        PluginSettingsData data = new PluginSettingsData(
                textBodyFontColorLabel.getForeground().getRGB(),
                (int) textBodyFontSizeSpinner.getValue(),
                apiCustomParamTextArea.getText(),
                enableErrorLogCheckBox.isSelected(),
                enableInLineModelCheckBox.isSelected()
        );

        // 保存到 SettingsManager
        settingsManager.saveSettings(data);
    }
}
