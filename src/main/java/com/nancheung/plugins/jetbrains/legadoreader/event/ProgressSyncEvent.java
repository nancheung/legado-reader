package com.nancheung.plugins.jetbrains.legadoreader.event;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 阅读进度同步事件
 * UI 层根据此事件展示同步状态提示
 *
 * @param eventId   事件唯一 ID
 * @param timestamp 事件时间戳
 * @param commandId 关联的指令 ID（进度同步不由指令直接触发，恒为 null）
 * @param status    同步状态
 * @author NanCheung
 */
public record ProgressSyncEvent(
        String eventId,
        long timestamp,
        @Nullable String commandId,
        SyncStatus status
) implements ReaderEvent {

    /**
     * 同步状态
     */
    public enum SyncStatus {
        /**
         * 同步中（已调度，防抖等待或请求进行中）
         */
        SYNCING,

        /**
         * 同步成功
         */
        SYNCED,

        /**
         * 同步失败
         */
        SYNC_FAILED
    }

    /**
     * 创建"同步中"事件
     */
    public static ProgressSyncEvent syncing() {
        return of(SyncStatus.SYNCING);
    }

    /**
     * 创建"同步成功"事件
     */
    public static ProgressSyncEvent synced() {
        return of(SyncStatus.SYNCED);
    }

    /**
     * 创建"同步失败"事件
     */
    public static ProgressSyncEvent syncFailed() {
        return of(SyncStatus.SYNC_FAILED);
    }

    private static ProgressSyncEvent of(SyncStatus status) {
        return new ProgressSyncEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), null, status);
    }
}
