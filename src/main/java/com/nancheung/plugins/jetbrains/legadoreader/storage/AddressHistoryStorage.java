package com.nancheung.plugins.jetbrains.legadoreader.storage;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址历史存储服务（Application Service）
 * 使用 IntelliJ Platform 的 PersistentStateComponent 进行持久化
 *
 * @author NanCheung
 */
@Service
@State(name = "LegadoReaderAddressHistory",storages = @Storage("nancheung-legadoReader-addressHistory.xml"))
public final class AddressHistoryStorage implements PersistentStateComponent<AddressHistoryStorage.State> {

    /**
     * 内部状态类，用于 XML 序列化
     * PersistentStateComponent 框架会自动检测字段变化并持久化
     */
    public static class State {
        public List<HistoryItemState> items = new ArrayList<>();
    }

    /**
     * 历史记录项状态类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItemState {
        public String address;
        public long lastAccessTime;
    }

    private State state = new State();

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static AddressHistoryStorage getInstance() {
        return ApplicationManager.getApplication().getService(AddressHistoryStorage.class);
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

    /**
     * 历史记录最大保存数量
     */
    public static final int MAX_SIZE = 4;


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

        State currentState = getState();

        // 移除已存在的相同地址
        currentState.items.removeIf(item -> item.address.equals(address));

        // 添加到最前面
        HistoryItemState newItem = new HistoryItemState(address, System.currentTimeMillis());
        currentState.items.addFirst(newItem);

        // 限制数量
        if (currentState.items.size() > MAX_SIZE) {
            currentState.items.removeLast();
        }
    }

    /**
     * 获取地址列表（按时间倒序）
     *
     * @return 地址列表
     */
    public List<String> getAddressList() {
        return getState().items.stream()
                .map(item -> item.address)
                .collect(Collectors.toList());
    }

    /**
     * 获取最近使用的地址
     *
     * @return 最近使用的地址，如果没有则返回 null
     */
    public String getMostRecent() {
        List<HistoryItemState> items = getState().items;
        return items.isEmpty() ? null : items.getFirst().address;
    }
}
