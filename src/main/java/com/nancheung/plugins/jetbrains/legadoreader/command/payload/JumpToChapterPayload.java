package com.nancheung.plugins.jetbrains.legadoreader.command.payload;

/**
 * 跳转章节参数
 *
 * @param chapterIndex 目标章节索引
 * @author NanCheung
 */
public record JumpToChapterPayload(
        int chapterIndex
) implements CommandPayload {
}
