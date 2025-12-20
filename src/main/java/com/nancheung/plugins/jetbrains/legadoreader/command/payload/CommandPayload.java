package com.nancheung.plugins.jetbrains.legadoreader.command.payload;

/**
 * 指令参数标记接口
 * 使用 sealed 接口确保类型安全
 *
 * @author NanCheung
 */
public sealed interface CommandPayload permits
        SelectBookPayload,
        JumpToChapterPayload,
        RefreshBookshelfPayload {
}
