package com.nancheung.plugins.jetbrains.legadoreader.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.model.AddressHistoryData;
import com.nancheung.plugins.jetbrains.legadoreader.model.AddressHistoryItem;
import com.nancheung.plugins.jetbrains.legadoreader.storage.AddressHistoryStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * 地址历史管理器（Application Service）
 * 管理地址历史的内存缓存和读写
 *
 * @author NanCheung
 */
@Service
public final class AddressHistoryManager {

    private AddressHistoryData currentHistory;

    /**
     * 构造函数，自动加载历史记录
     */
    public AddressHistoryManager() {
        this.currentHistory = AddressHistoryStorage.getInstance().load();
    }

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static AddressHistoryManager getInstance() {
        return ApplicationManager.getApplication().getService(AddressHistoryManager.class);
    }

    /**
     * 添加地址到历史记录
     * 自动去重、排序、限制数量
     *
     * @param address 地址
     */
    public void addAddress(String address) {
        if (address == null || address.isEmpty()) {
            return;
        }

        List<AddressHistoryItem> items = new ArrayList<>(currentHistory.items());

        // 移除已存在的相同地址
        items.removeIf(item -> item.address().equals(address));

        // 添加到最前面
        items.add(0, new AddressHistoryItem(address));

        // 限制数量
        if (items.size() > AddressHistoryData.MAX_SIZE) {
            items = items.subList(0, AddressHistoryData.MAX_SIZE);
        }

        // 更新缓存和持久化
        currentHistory = new AddressHistoryData(items);
        AddressHistoryStorage.getInstance().save(currentHistory);
    }

    /**
     * 获取地址列表（按时间倒序）
     *
     * @return 地址列表
     */
    public List<String> getAddressList() {
        return currentHistory.items().stream()
                .map(AddressHistoryItem::address)
                .toList();
    }

    /**
     * 获取最近使用的地址
     *
     * @return 最近使用的地址，如果没有则返回 null
     */
    public String getMostRecent() {
        return currentHistory.items().isEmpty() ? null :
                currentHistory.items().get(0).address();
    }

    /**
     * 清空历史记录
     */
    public void clear() {
        currentHistory = AddressHistoryData.empty();
        AddressHistoryStorage.getInstance().save(currentHistory);
    }

    /**
     * 获取历史记录数量
     *
     * @return 历史记录数量
     */
    public int size() {
        return currentHistory.items().size();
    }
}
