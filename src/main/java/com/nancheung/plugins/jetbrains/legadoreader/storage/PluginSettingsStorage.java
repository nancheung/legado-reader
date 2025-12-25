package com.nancheung.plugins.jetbrains.legadoreader.storage;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 插件设置存储服务（Application Service）
 * 使用 IntelliJ Platform 的 PersistentStateComponent 进行持久化
 *
 * @author NanCheung
 */
@Service
@State(name = "LegadoReaderSettings", storages = @Storage("nancheung-legadoReader-settings.xml"))
public final class PluginSettingsStorage implements PersistentStateComponent<PluginSettingsStorage.State> {

    /**
     * 内部状态类，用于 XML 序列化
     * PersistentStateComponent 框架会自动检测字段变化并持久化
     */
    public static class State {
        /**
         * 正文字体颜色 RGB 值
         */
        public Integer textBodyFontColorRgb;
        /**
         * 正文字体大小
         */
        public Integer textBodyFontSize;
        /**
         * 正文字体名称
         */
        public String textBodyFontFamily;
        /**
         * 正文字体行高倍数
         */
        public Double textBodyLineHeight;
        /**
         * API 自定义参数
         */
        public String apiCustomParam;
        /**
         * 是否启用错误日志
         */
        public Boolean enableErrorLog = false;
        /**
         * 是否启用行内模式
         */
        public Boolean enableShowBodyInLine = false;
    }

    private State state = new State();

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static PluginSettingsStorage getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettingsStorage.class);
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    // ==================== 便捷访问方法 ====================

    /**
     * 获取正文字体颜色
     *
     * @return 字体颜色
     */
    public Color getTextBodyFontColor() {
        Integer rgb = getState().textBodyFontColorRgb;
        return rgb != null ? new Color(rgb) : Color.BLACK;
    }

    /**
     * 获取正文字体
     *
     * @return 字体
     */
    public Font getTextBodyFont() {
        State state = getState();
        String family = getTextBodyFontFamily();
        Integer size = state.textBodyFontSize;

        if (size == null || size == 0) {
            return new JLabel().getFont();
        }

        return new Font(family, Font.PLAIN, size);
    }

    /**
     * 获取 API 自定义参数
     * 格式：参数名:@参数值（每行一个）
     *
     * @return 参数 Map
     */
    public Map<String, Object> getApiCustomParam() {
        String param = getState().apiCustomParam;
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
                .collect(Collectors.toMap(l -> l.getFirst(), l -> l.get(1), (a, b) -> b));
    }

    /**
     * 切换阅读模式显示/隐藏
     * 全局开关，同时影响 ToolWindow 和 EditorLine 两种阅读模式
     *
     * @return 切换后的状态（true=启用，false=禁用）
     */
    public boolean toggleReadingMode() {
        State currentState = getState();
        boolean newState = !Boolean.TRUE.equals(currentState.enableShowBodyInLine);

        // 直接修改 State 字段，框架会自动持久化
        currentState.enableShowBodyInLine = newState;

        return newState;
    }

    /**
     * 获取正文字体名称
     * 如果未设置，返回 IDE 编辑器字体
     *
     * @return 字体名称
     */
    public String getTextBodyFontFamily() {
        String family = getState().textBodyFontFamily;
        if (family == null || family.isEmpty()) {
            try {
                return com.intellij.openapi.editor.colors.EditorColorsManager.getInstance()
                        .getGlobalScheme()
                        .getFont(com.intellij.openapi.editor.colors.EditorFontType.PLAIN)
                        .getFamily();
            } catch (Exception e) {
                return new JLabel().getFont().getFamily();
            }
        }
        return family;
    }

    /**
     * 获取正文字体行高
     * 如果未设置或超出范围，返回默认值 1.5
     *
     * @return 行高倍数
     */
    public double getTextBodyLineHeight() {
        Double lineHeight = getState().textBodyLineHeight;
        if (lineHeight == null || lineHeight < 0.5 || lineHeight > 3.0) {
            return 1.5;
        }
        return lineHeight;
    }
}
