package com.nancheung.plugins.jetbrains.legadoreader.model;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;

import java.util.List;

/**
 * 阅读会话（不可变）
 *
 * @param book                当前书籍
 * @param chapters            章节列表
 * @param currentChapterIndex 当前章节索引
 * @param currentContent      当前章节内容
 * @author NanCheung
 */
public record ReadingSession(
        BookDTO book,
        List<BookChapterDTO> chapters,
        int currentChapterIndex,
        String currentContent
) {
    /**
     * 切换到下一章
     *
     * @return 新的会话对象
     */
    public ReadingSession nextChapter() {
        return new ReadingSession(book, chapters, currentChapterIndex + 1, null);
    }

    /**
     * 切换到上一章
     *
     * @return 新的会话对象
     */
    public ReadingSession previousChapter() {
        return new ReadingSession(book, chapters, currentChapterIndex - 1, null);
    }

    /**
     * 更新章节索引
     *
     * @param newIndex 新的章节索引
     * @return 新的会话对象
     */
    public ReadingSession withChapterIndex(int newIndex) {
        return new ReadingSession(book, chapters, newIndex, null);
    }

    /**
     * 更新章节内容
     *
     * @param content 章节内容
     * @return 新的会话对象
     */
    public ReadingSession withContent(String content) {
        return new ReadingSession(book, chapters, currentChapterIndex, content);
    }

    /**
     * 获取当前章节
     *
     * @return 当前章节，如果索引无效则返回 null
     */
    public BookChapterDTO getCurrentChapter() {
        if (chapters == null || currentChapterIndex < 0 || currentChapterIndex >= chapters.size()) {
            return null;
        }
        return chapters.get(currentChapterIndex);
    }
}
