package com.nancheung.plugins.jetbrains.legadoreader.model;

/**
 * 地址历史记录项
 *
 * @param address         地址
 * @param lastAccessTime  最后访问时间戳
 * @param alias           别名（预留字段）
 * @author NanCheung
 */
public record AddressHistoryItem(
        String address,
        long lastAccessTime,
        String alias
) {
    /**
     * 创建新的历史记录项（使用当前时间戳）
     *
     * @param address 地址
     */
    public AddressHistoryItem(String address) {
        this(address, System.currentTimeMillis(), null);
    }
}
