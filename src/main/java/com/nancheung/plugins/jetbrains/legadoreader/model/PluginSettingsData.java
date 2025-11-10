package com.nancheung.plugins.jetbrains.legadoreader.model;

/**
 * 插件设置数据
 *
 * @param textBodyFontColorRgb 正文字体颜色 RGB 值
 * @param textBodyFontSize     正文字体大小
 * @param apiCustomParam       API 自定义参数
 * @param enableErrorLog       是否启用错误日志
 * @author NanCheung
 */
public record PluginSettingsData(
        Integer textBodyFontColorRgb,
        Integer textBodyFontSize,
        String apiCustomParam,
        Boolean enableErrorLog
) {
    /**
     * 获取默认设置
     *
     * @return 默认设置
     */
    public static PluginSettingsData defaults() {
        return new PluginSettingsData(null, null, null, false);
    }
}
