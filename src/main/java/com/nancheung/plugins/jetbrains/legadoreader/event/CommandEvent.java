package com.nancheung.plugins.jetbrains.legadoreader.event;

import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandResult;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 指令生命周期事件
 * 用于 UI 层显示 loading 状态和执行结果
 *
 * @param eventId     事件唯一 ID
 * @param timestamp   事件时间戳
 * @param commandId   关联的指令 ID
 * @param commandType 指令类型
 * @param status      执行状态
 * @param message     消息内容（可选，用于显示提示）
 * @author NanCheung
 */
public record CommandEvent(
        String eventId,
        long timestamp,
        String commandId,
        CommandType commandType,
        CommandResult.ExecutionStatus status,
        @Nullable String message
) implements ReaderEvent {

    /**
     * 创建"指令开始"事件
     */
    public static CommandEvent started(Command cmd) {
        return new CommandEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                cmd.id(),
                cmd.type(),
                CommandResult.ExecutionStatus.STARTED,
                null
        );
    }

    /**
     * 创建"指令完成"事件
     */
    public static CommandEvent completed(Command cmd, @Nullable String message) {
        return new CommandEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                cmd.id(),
                cmd.type(),
                CommandResult.ExecutionStatus.SUCCESS,
                message
        );
    }

    /**
     * 创建"指令失败"事件
     */
    public static CommandEvent failed(Command cmd, String errorMessage) {
        return new CommandEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                cmd.id(),
                cmd.type(),
                CommandResult.ExecutionStatus.FAILED,
                errorMessage
        );
    }

    /**
     * 创建"指令取消"事件
     */
    public static CommandEvent cancelled(Command cmd) {
        return new CommandEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                cmd.id(),
                cmd.type(),
                CommandResult.ExecutionStatus.CANCELLED,
                null
        );
    }
}
