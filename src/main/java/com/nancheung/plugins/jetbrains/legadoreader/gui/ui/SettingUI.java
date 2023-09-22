package com.nancheung.plugins.jetbrains.legadoreader.gui.ui;

import cn.hutool.core.util.StrUtil;
import com.intellij.ui.ColorPicker;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.properties.GlobalSettingProperties;
import com.nancheung.plugins.jetbrains.legadoreader.properties.PropertiesFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingUI {
    private JPanel rootPanel;

    private JLabel textBodyFontColorLabel;

    private JSpinner textBodyFontSizeSpinner;
    private JTextArea apiCustomParamTextArea;
    private JCheckBox enableErrorLogCheckBox;

    private static final GlobalSettingProperties GLOBAL_SETTING_PROPERTIES = PropertiesFactory.getGlobalSetting();

    public SettingUI() {
        // 正文大小输入范围
        textBodyFontSizeSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
        // 读取已有配置
        readSettings();
        // 保存设置
        saveSettings();

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

    /**
     * 读取本地配置
     */
    public void readSettings() {
        Color textBodyFontColor = GLOBAL_SETTING_PROPERTIES.getTextBodyFontColor();
        if (textBodyFontColor != null) {
            textBodyFontColorLabel.setForeground(new JBColor(textBodyFontColor, textBodyFontColor));
        }

        Font textBodyFont = GLOBAL_SETTING_PROPERTIES.getTextBodyFont();
        if (textBodyFont != null && textBodyFont.getSize() > 0) {
            textBodyFontSizeSpinner.setValue(textBodyFont.getSize());
        }

        Map<String, Object> apiCustomParam = GLOBAL_SETTING_PROPERTIES.getApiCustomParam();
        if (apiCustomParam != null) {
            String apiCustomParamString = apiCustomParam.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ":@" + entry.getValue())
                    .collect(Collectors.joining("\n"));
            apiCustomParamTextArea.setText(apiCustomParamString);
        }

        enableErrorLogCheckBox.setSelected(GLOBAL_SETTING_PROPERTIES.isEnableErrorLog());
    }

    /**
     * 保存设置
     */
    public void saveSettings() {
        Font defaultFont = textBodyFontSizeSpinner.getFont();
        int fontSize = (int) textBodyFontSizeSpinner.getValue();
        if (fontSize == 0) {
            fontSize = defaultFont.getSize();
        }

        GLOBAL_SETTING_PROPERTIES.setTextBodyFont(new Font(defaultFont.getName(), Font.PLAIN, fontSize))
                .setTextBodyFontColor(new JBColor(textBodyFontColorLabel.getForeground(), textBodyFontColorLabel.getForeground()))
                .setEnableErrorLog(enableErrorLogCheckBox.isSelected())
                .setApiCustomParam(buildApiCustomParam(apiCustomParamTextArea.getText()))
                .save();
    }

    private Map<String, Object> buildApiCustomParam(String apiCustomParam) {
        if (StrUtil.isBlank(apiCustomParam)) {
            return Map.of();
        }

        // 按照换行符分割取出每行数据，按照 :@ 分割取出kv，转成map
        return StrUtil.split(apiCustomParam, "\n")
                .stream()
                .filter(StrUtil::isNotEmpty)
                .filter(s -> s.contains(":@"))
                .map(s -> StrUtil.split(s, ":@"))
                .collect(Collectors.toMap(l -> l.get(0), l -> l.get(1), (a, b) -> b));
    }
}
