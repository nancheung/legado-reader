package com.nancheung.plugins.jetbrains.legadoreader.event;

import org.jetbrains.annotations.Nullable;

/**
 * 阅读器事件标记接口（sealed）
 * 所有事件都是不可变 Record
 * 使用 sealed 接口确保类型安全，只允许预定义的事件类型
 *
 * @author NanCheung
 */
public sealed interface ReaderEvent permits
        CommandEvent,
        BookshelfEvent,
        ReadingEvent,
        PaginationEvent {

    /**
     * 事件唯一 ID
     */
    String eventId();

    /**
     * 事件时间戳
     */
    long timestamp();

    /**
     * 关联的指令 ID（可选）
     * 用于追踪事件是由哪个指令触发的
     */
    @Nullable
    String commandId();
}
