package com.nancheung.plugins.jetbrains.legadoreader.event;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 书架相关事件
 *
 * @param eventId   事件唯一 ID
 * @param timestamp 事件时间戳
 * @param commandId 关联的指令 ID
 * @param type      书架事件类型
 * @param books     书籍列表（加载成功时）
 * @param error     错误信息（加载失败时）
 * @author NanCheung
 */
public record BookshelfEvent(
        String eventId,
        long timestamp,
        @Nullable String commandId,
        BookshelfEventType type,
        @Nullable List<BookDTO> books,
        @Nullable Throwable error
) implements ReaderEvent {

    /**
     * 书架事件类型
     */
    public enum BookshelfEventType {
        /**
         * 开始加载书架
         */
        LOADING,

        /**
         * 书架加载成功
         */
        LOADED,

        /**
         * 书架加载失败
         */
        LOAD_FAILED
    }

    /**
     * 创建"书架加载中"事件
     */
    public static BookshelfEvent loading(@Nullable String commandId) {
        return new BookshelfEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                BookshelfEventType.LOADING,
                null,
                null
        );
    }

    /**
     * 创建"书架加载成功"事件
     */
    public static BookshelfEvent loaded(@Nullable String commandId, List<BookDTO> books) {
        return new BookshelfEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                BookshelfEventType.LOADED,
                books,
                null
        );
    }

    /**
     * 创建"书架加载失败"事件
     */
    public static BookshelfEvent loadFailed(@Nullable String commandId, Throwable error) {
        return new BookshelfEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                BookshelfEventType.LOAD_FAILED,
                null,
                error
        );
    }
}
