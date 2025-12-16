package com.nancheung.plugins.jetbrains.legadoreader.event;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 阅读内容事件（不可变）
 * 章节加载相关事件
 *
 * @param eventId         事件唯一 ID
 * @param timestamp       事件时间戳
 * @param commandId       关联的指令 ID
 * @param type            阅读事件类型
 * @param direction       章节切换方向
 * @param book            书籍信息
 * @param chapter         章节信息
 * @param content         章节内容（可能为 null）
 * @param chapterPosition 章节内光标位置
 * @param error           错误信息（可能为 null）
 * @author NanCheung
 */
public record ReadingEvent(
        String eventId,
        long timestamp,
        @Nullable String commandId,
        ReadingEventType type,
        Direction direction,
        @Nullable BookDTO book,
        @Nullable BookChapterDTO chapter,
        @Nullable String content,
        int chapterPosition,
        @Nullable Throwable error
) implements ReaderEvent {

    /**
     * 阅读事件类型
     */
    public enum ReadingEventType {
        /**
         * 开始加载章节
         */
        CHAPTER_LOADING,

        /**
         * 章节加载成功
         */
        CHAPTER_LOADED,

        /**
         * 章节加载失败
         */
        CHAPTER_LOAD_FAILED,

        /**
         * 阅读会话结束（返回书架）
         */
        SESSION_ENDED
    }

    /**
     * 章节切换方向
     */
    public enum Direction {
        /**
         * 下一章
         */
        NEXT,

        /**
         * 上一章
         */
        PREVIOUS,

        /**
         * 跳转（从书架点击或目录跳转）
         */
        JUMP
    }

    /**
     * 创建"开始加载章节"事件
     */
    public static ReadingEvent chapterLoading(@Nullable String commandId, BookDTO book, BookChapterDTO chapter, Direction direction) {
        return new ReadingEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                ReadingEventType.CHAPTER_LOADING,
                direction,
                book,
                chapter,
                null,
                0,
                null
        );
    }

    /**
     * 创建"章节加载成功"事件
     */
    public static ReadingEvent chapterLoaded(
            @Nullable String commandId,
            BookDTO book,
            BookChapterDTO chapter,
            String content,
            int chapterPosition,
            Direction direction) {
        return new ReadingEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                ReadingEventType.CHAPTER_LOADED,
                direction,
                book,
                chapter,
                content,
                chapterPosition,
                null
        );
    }

    /**
     * 创建"章节加载失败"事件
     */
    public static ReadingEvent chapterLoadFailed(
            @Nullable String commandId,
            BookDTO book,
            BookChapterDTO chapter,
            Throwable error,
            Direction direction) {
        return new ReadingEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                ReadingEventType.CHAPTER_LOAD_FAILED,
                direction,
                book,
                chapter,
                null,
                0,
                error
        );
    }

    /**
     * 创建"会话结束"事件（返回书架）
     */
    public static ReadingEvent sessionEnded(@Nullable String commandId) {
        return new ReadingEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                ReadingEventType.SESSION_ENDED,
                Direction.JUMP,
                null,
                null,
                null,
                0,
                null
        );
    }

    // ========== 向后兼容方法（保留旧 API） ==========

    /**
     * @deprecated 请使用 {@link #chapterLoading(String, BookDTO, BookChapterDTO, Direction)}
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static ReadingEvent loadingStarted(BookDTO book, BookChapterDTO chapter, Direction direction) {
        return chapterLoading(null, book, chapter, direction);
    }

    /**
     * @deprecated 请使用 {@link #chapterLoaded(String, BookDTO, BookChapterDTO, String, int, Direction)}
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static ReadingEvent loadingSuccess(BookDTO book, BookChapterDTO chapter, String content, int chapterPosition, Direction direction) {
        return chapterLoaded(null, book, chapter, content, chapterPosition, direction);
    }

    /**
     * @deprecated 请使用 {@link #chapterLoadFailed(String, BookDTO, BookChapterDTO, Throwable, Direction)}
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static ReadingEvent loadingFailed(BookDTO book, BookChapterDTO chapter, Throwable error, Direction direction) {
        return chapterLoadFailed(null, book, chapter, error, direction);
    }

    /**
     * 向后兼容：获取旧的状态枚举
     *
     * @deprecated 请使用 {@link #type()}
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public Status status() {
        return switch (type) {
            case CHAPTER_LOADING -> Status.LOADING_STARTED;
            case CHAPTER_LOADED -> Status.LOADING_SUCCESS;
            case CHAPTER_LOAD_FAILED -> Status.LOADING_FAILED;
            default -> Status.LOADING_FAILED;
        };
    }

    /**
     * 旧的状态枚举（向后兼容）
     *
     * @deprecated 请使用 {@link ReadingEventType}
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public enum Status {
        LOADING_STARTED,
        LOADING_SUCCESS,
        LOADING_FAILED
    }
}
