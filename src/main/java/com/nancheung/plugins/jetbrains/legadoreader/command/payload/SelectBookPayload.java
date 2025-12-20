package com.nancheung.plugins.jetbrains.legadoreader.command.payload;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;

/**
 * 选择书籍参数
 *
 * @param book         书籍信息
 * @param chapterIndex 章节索引
 * @author NanCheung
 */
public record SelectBookPayload(
        BookDTO book,
        int chapterIndex
) implements CommandPayload {
}
