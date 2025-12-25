package com.nancheung.plugins.jetbrains.legadoreader.gui.ui;

import com.intellij.ui.ColorPicker;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Getter
public class SettingUI {
    private JPanel rootPanel;

    private JLabel textBodyFontColorLabel;

    private JSpinner textBodyFontSizeSpinner;
    private JTextArea apiCustomParamTextArea;
    private JCheckBox enableErrorLogCheckBox;
    private JCheckBox enableInLineModelCheckBox;


    public SettingUI() {
        // 正文字体大小限制输入范围
        textBodyFontSizeSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));

        // 拒绝非法输入，自动提交合法输入
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) textBodyFontSizeSpinner.getEditor();
        if (editor.getTextField().getFormatter() instanceof DefaultFormatter df) {
            df.setAllowsInvalid(false);
            df.setCommitsOnValidEdit(true);
        }

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
    
    public void readSettings(PluginSettingsStorage.State state) {
        if (state == null) {
            return;
        }

        if (state.textBodyFontColorRgb != null) {
            textBodyFontColorLabel.setForeground(new JBColor(new Color(state.textBodyFontColorRgb), new Color(state.textBodyFontColorRgb)));
        }

        if (state.textBodyFontSize != null && state.textBodyFontSize >= 0) {
            textBodyFontSizeSpinner.setValue(state.textBodyFontSize);
        }

        if (state.apiCustomParam != null && !state.apiCustomParam.isEmpty()) {
            apiCustomParamTextArea.setText(state.apiCustomParam);
        }

        enableErrorLogCheckBox.setSelected(Boolean.TRUE.equals(state.enableErrorLog));
        enableInLineModelCheckBox.setSelected(Boolean.TRUE.equals(state.enableShowBodyInLine));
    }
}
