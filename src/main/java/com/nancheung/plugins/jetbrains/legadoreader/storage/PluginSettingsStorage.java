package com.nancheung.plugins.jetbrains.legadoreader.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.nancheung.plugins.jetbrains.legadoreader.model.PluginSettingsData;

/**
 * 插件设置存储服务（Application Service）
 * 负责插件设置的 JSON 序列化和持久化
 *
 * @author NanCheung
 */
@Service
public final class PluginSettingsStorage {

    private static final Logger log = Logger.getInstance(PluginSettingsStorage.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static PluginSettingsStorage getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettingsStorage.class);
    }

    /**
     * 保存设置到持久化存储
     *
     * @param data 设置数据
     */
    public void save(PluginSettingsData data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            PropertiesComponent.getInstance().setValue(StorageKeys.SETTINGS, json);
        } catch (JsonProcessingException e) {
            log.error("保存插件设置失败", e);
        }
    }

    /**
     * 从持久化存储加载设置
     *
     * @return 设置数据，如果加载失败则返回默认值
     */
    public PluginSettingsData load() {
        String json = PropertiesComponent.getInstance().getValue(StorageKeys.SETTINGS);
        if (json == null || json.isEmpty()) {
            return PluginSettingsData.defaults();
        }

        try {
            return objectMapper.readValue(json, PluginSettingsData.class);
        } catch (JsonProcessingException e) {
            log.warn("加载插件设置失败，使用默认值", e);
            return PluginSettingsData.defaults();
        }
    }
}
