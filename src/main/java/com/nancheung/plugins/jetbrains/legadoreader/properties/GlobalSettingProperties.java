package com.nancheung.plugins.jetbrains.legadoreader.properties;

import com.intellij.ui.JBColor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.awt.*;
import java.util.Map;

/**
 * 全局设置
 *
 * @author NanCheung
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class GlobalSettingProperties extends AbstractProperties {
    /**
     * API自定义参数
     */
    private Map<String, Object> apiCustomParam;

    /**
     * 是否启用错误日志
     */
    private boolean enableErrorLog = false;

    /**
     * 正文字体
     */
    private Font textBodyFont;

    /**
     * 正文字体颜色
     */
    private JBColor textBodyFontColor;

    /**
     * 是否启用在代码行中显示正文（实验性）
     */
    private boolean enableShowBodyInLine = false;

    @Override
    protected String name() {
        return "global-setting";
    }
}
