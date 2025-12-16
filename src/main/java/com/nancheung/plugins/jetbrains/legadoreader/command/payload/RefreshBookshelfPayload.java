package com.nancheung.plugins.jetbrains.legadoreader.command.payload;

import org.jetbrains.annotations.Nullable;

/**
 * 刷新书架参数
 *
 * @param address 服务器地址（null 表示使用当前地址）
 * @author NanCheung
 */
public record RefreshBookshelfPayload(
        @Nullable String address
) implements CommandPayload {
}
