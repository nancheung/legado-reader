package com.nancheung.plugins.jetbrains.legadoreader.gui.ui;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.ColorPicker;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

@Getter
public class SettingUI {
    private JPanel rootPanel;

    private JLabel textBodyFontColorLabel;

    private JSpinner textBodyFontSizeSpinner;
    private JTextArea apiCustomParamTextArea;
    private JCheckBox enableErrorLogCheckBox;
    private JCheckBox enableInLineModelCheckBox;

    // 新增组件
    private JComboBox<String> textBodyFontFamilyComboBox;
    private JSpinner textBodyLineHeightSpinner;
    private JTextPane fontPreviewPane;

    // 新增常量
    private static final String FONT_PREVIEW_TEXT = """
            字体预览 Font Preview

            Designed to provide inspiration and improve productivity during the coding process.
            
            Legado Reader是 开源阅读APP 的Jetbrains IDE插件版，旨在随时随地在IDE中进行阅读，为编码过程带来灵感和效率的提升。
            """;


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

        // 新增：初始化字体选择器
        initFontFamilyComboBox();

        // 新增：初始化行高输入框
        initLineHeightSpinner();

        // 新增：初始化预览面板
        initFontPreviewPane();

        // 新增：设置实时预览监听器
        setupPreviewListeners();
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
    
    private void initFontFamilyComboBox() {
        // 获取系统字体列表，并重排序
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilies = ge.getAvailableFontFamilyNames();
        Arrays.sort(fontFamilies);

        textBodyFontFamilyComboBox.setModel(new DefaultComboBoxModel<>(fontFamilies));

        // 使用字体自身显示
        textBodyFontFamilyComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String fontFamily) {
                    setFont(new Font(fontFamily, Font.PLAIN, getFont().getSize()));
                }
                return this;
            }
        });
    }

    private void initLineHeightSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(1.5, 0.5, 3.0, 0.1);
        textBodyLineHeightSpinner.setModel(model);

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(textBodyLineHeightSpinner, "0.0");
        textBodyLineHeightSpinner.setEditor(editor);

        JFormattedTextField textField = editor.getTextField();
        if (textField.getFormatter() instanceof DefaultFormatter df) {
            df.setAllowsInvalid(false);
            df.setCommitsOnValidEdit(true);
        }
    }

    private void initFontPreviewPane() {
        fontPreviewPane.setText(FONT_PREVIEW_TEXT);
        fontPreviewPane.setEditable(false);
        fontPreviewPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("字体效果预览"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void setupPreviewListeners() {
        textBodyFontColorLabel.addPropertyChangeListener("foreground", evt ->
                SwingUtilities.invokeLater(this::updateFontPreview));
        textBodyFontFamilyComboBox.addActionListener(e ->
                SwingUtilities.invokeLater(this::updateFontPreview));
        textBodyFontSizeSpinner.addChangeListener(e ->
                SwingUtilities.invokeLater(this::updateFontPreview));
        textBodyLineHeightSpinner.addChangeListener(e ->
                SwingUtilities.invokeLater(this::updateFontPreview));
    }

    private void updateFontPreview() {
        try {
            Color fontColor = textBodyFontColorLabel.getForeground();
            String fontFamily = (String) textBodyFontFamilyComboBox.getSelectedItem();
            int fontSize = (int) textBodyFontSizeSpinner.getValue();
            double lineHeight = (double) textBodyLineHeightSpinner.getValue();

            if (fontSize == 0) {
                fontSize = new JLabel().getFont().getSize();
            }

            fontPreviewPane.setForeground(fontColor);
            fontPreviewPane.setFont(new Font(fontFamily, Font.PLAIN, fontSize));

            applyLineHeight(fontPreviewPane, lineHeight);
        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void applyLineHeight(JTextPane textPane, double lineHeight) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        float lineSpacing = (float) (lineHeight - 1.0);
        StyleConstants.setLineSpacing(attrs, lineSpacing);

        StyledDocument doc = textPane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
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

        // 新增：读取字体名称
        String fontFamily = state.textBodyFontFamily;
        if (fontFamily == null || fontFamily.isEmpty()) {
            try {
                fontFamily = EditorColorsManager.getInstance()
                        .getGlobalScheme()
                        .getFont(EditorFontType.PLAIN)
                        .getFamily();
            } catch (Exception e) {
                fontFamily = new JLabel().getFont().getFamily();
            }
        }
        textBodyFontFamilyComboBox.setSelectedItem(fontFamily);

        // 新增：读取行高（需要空值检查）
        if (state.textBodyLineHeight != null) {
            textBodyLineHeightSpinner.setValue(state.textBodyLineHeight);
        }

        // 更新预览
        SwingUtilities.invokeLater(this::updateFontPreview);
    }
}
