package com.nancheung.plugins.jetbrains.legadoreader.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 地址历史记录数据
 *
 * @param items 历史记录列表
 * @author NanCheung
 */
public record AddressHistoryData(
        List<AddressHistoryItem> items
) {
    /**
     * 最大保存数量
     */
    public static final int MAX_SIZE = 4;

    /**
     * 创建空的历史记录
     *
     * @return 空的历史记录
     */
    public static AddressHistoryData empty() {
        return new AddressHistoryData(new ArrayList<>());
    }
}
