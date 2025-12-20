package com.nancheung.plugins.jetbrains.legadoreader.event;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 分页事件
 * UI 层根据此事件更新当前页显示
 *
 * @param eventId     事件唯一 ID
 * @param timestamp   事件时间戳
 * @param commandId   关联的指令 ID
 * @param type        分页事件类型
 * @param currentPage 当前页码（从 1 开始）
 * @param totalPages  总页数
 * @param pageContent 当前页内容（可选）
 * @author NanCheung
 */
public record PaginationEvent(
        String eventId,
        long timestamp,
        @Nullable String commandId,
        PaginationEventType type,
        int currentPage,
        int totalPages,
        @Nullable String pageContent
) implements ReaderEvent {

    /**
     * 分页事件类型
     */
    public enum PaginationEventType {
        /**
         * 分页完成（章节内容已分页）
         */
        PAGINATED,

        /**
         * 页码变更（翻页）
         */
        PAGE_CHANGED
    }

    /**
     * 创建"分页完成"事件
     */
    public static PaginationEvent paginated(@Nullable String commandId, int totalPages, int currentPage, String content) {
        return new PaginationEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                commandId,
                PaginationEventType.PAGINATED,
                currentPage,
                totalPages,
                content
        );
    }

    /**
     * 创建"页码变更"事件
     */
    public static PaginationEvent pageChanged(int currentPage, int totalPages, String content) {
        return new PaginationEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                null,
                PaginationEventType.PAGE_CHANGED,
                currentPage,
                totalPages,
                content
        );
    }
}
