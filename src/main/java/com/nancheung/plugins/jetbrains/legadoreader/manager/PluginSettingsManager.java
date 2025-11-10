package com.nancheung.plugins.jetbrains.legadoreader.manager;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.model.PluginSettingsData;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 插件设置管理器（Application Service）
 * 管理插件设置的内存缓存和读写
 *
 * @author NanCheung
 */
@Service
public final class PluginSettingsManager {

    private PluginSettingsData currentSettings;

    /**
     * 构造函数，自动加载配置
     */
    public PluginSettingsManager() {
        this.currentSettings = PluginSettingsStorage.getInstance().load();
    }

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static PluginSettingsManager getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettingsManager.class);
    }

    /**
     * 获取当前设置
     *
     * @return 当前设置
     */
    public PluginSettingsData getSettings() {
        return currentSettings;
    }

    /**
     * 保存设置
     *
     * @param settings 新的设置
     */
    public void saveSettings(PluginSettingsData settings) {
        this.currentSettings = settings;
        PluginSettingsStorage.getInstance().save(settings);
    }

    /**
     * 获取正文字体颜色
     *
     * @return 字体颜色
     */
    public Color getTextBodyFontColor() {
        Integer rgb = currentSettings.textBodyFontColorRgb();
        return rgb != null ? new Color(rgb) : Color.BLACK;
    }

    /**
     * 获取正文字体
     *
     * @return 字体
     */
    public Font getTextBodyFont() {
        Integer size = currentSettings.textBodyFontSize();
        if (size == null || size == 0) {
            return new JLabel().getFont();
        }
        return new Font(Font.DIALOG, Font.PLAIN, size);
    }

    /**
     * 获取 API 自定义参数
     * 格式：参数名:@参数值（每行一个）
     *
     * @return 参数 Map
     */
    public Map<String, Object> getApiCustomParam() {
        String param = currentSettings.apiCustomParam();
        if (StrUtil.isBlank(param)) {
            return Map.of();
        }

        // 按照回车符分割，取出所有自定义参数
        List<String> apiCustomParamList = StrUtil.split(param, "\n");

        // 按照 :@ 分割，取出参数名和参数值,转成map
        return apiCustomParamList.stream()
                .filter(StrUtil::isNotEmpty)
                .filter(s -> s.contains(StrPool.COLON + StrPool.AT))
                .map(s -> StrUtil.split(s, StrPool.COLON + StrPool.AT))
                .collect(Collectors.toMap(l -> l.get(0), l -> l.get(1), (a, b) -> b));
    }

    /**
     * 是否启用错误日志
     *
     * @return 是否启用
     */
    public boolean isEnableErrorLog() {
        return Boolean.TRUE.equals(currentSettings.enableErrorLog());
    }
}
