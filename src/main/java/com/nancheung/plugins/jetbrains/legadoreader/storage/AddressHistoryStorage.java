package com.nancheung.plugins.jetbrains.legadoreader.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.nancheung.plugins.jetbrains.legadoreader.model.AddressHistoryData;

/**
 * 地址历史存储服务（Application Service）
 * 负责地址历史的 JSON 序列化和持久化
 *
 * @author NanCheung
 */
@Service
public final class AddressHistoryStorage {

    private static final Logger log = Logger.getInstance(AddressHistoryStorage.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static AddressHistoryStorage getInstance() {
        return ApplicationManager.getApplication().getService(AddressHistoryStorage.class);
    }

    /**
     * 保存历史记录到持久化存储
     *
     * @param data 历史记录数据
     */
    public void save(AddressHistoryData data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            PropertiesComponent.getInstance().setValue(StorageKeys.ADDRESS_HISTORY, json);
        } catch (JsonProcessingException e) {
            log.error("保存地址历史失败", e);
        }
    }

    /**
     * 从持久化存储加载历史记录
     *
     * @return 历史记录数据，如果加载失败则返回空列表
     */
    public AddressHistoryData load() {
        String json = PropertiesComponent.getInstance().getValue(StorageKeys.ADDRESS_HISTORY);
        if (json == null || json.isEmpty()) {
            return AddressHistoryData.empty();
        }

        try {
            return objectMapper.readValue(json, AddressHistoryData.class);
        } catch (JsonProcessingException e) {
            log.warn("加载地址历史失败，使用空列表", e);
            return AddressHistoryData.empty();
        }
    }
}
