package com.nancheung.plugins.jetbrains.legadoreader.command;

import org.jetbrains.annotations.Nullable;

/**
 * 指令执行结果（不可变）
 *
 * @param commandId   关联的指令 ID
 * @param commandType 指令类型
 * @param status      执行状态
 * @param data        结果数据（成功时）
 * @param error       错误信息（失败时）
 * @param timestamp   结果生成时间戳
 * @author NanCheung
 */
public record CommandResult(
        String commandId,
        CommandType commandType,
        ExecutionStatus status,
        @Nullable Object data,
        @Nullable Throwable error,
        long timestamp
) {
    /**
     * 指令执行状态
     */
    public enum ExecutionStatus {
        /**
         * 指令已接收，开始执行
         */
        STARTED,

        /**
         * 执行成功
         */
        SUCCESS,

        /**
         * 执行失败
         */
        FAILED,

        /**
         * 已取消（如快速连续点击）
         */
        CANCELLED
    }

    /**
     * 创建"开始执行"结果
     */
    public static CommandResult started(Command cmd) {
        return new CommandResult(
                cmd.id(),
                cmd.type(),
                ExecutionStatus.STARTED,
                null,
                null,
                System.currentTimeMillis()
        );
    }

    /**
     * 创建"执行成功"结果
     */
    public static CommandResult success(Command cmd, @Nullable Object data) {
        return new CommandResult(
                cmd.id(),
                cmd.type(),
                ExecutionStatus.SUCCESS,
                data,
                null,
                System.currentTimeMillis()
        );
    }

    /**
     * 创建"执行失败"结果
     */
    public static CommandResult failed(Command cmd, Throwable error) {
        return new CommandResult(
                cmd.id(),
                cmd.type(),
                ExecutionStatus.FAILED,
                null,
                error,
                System.currentTimeMillis()
        );
    }

    /**
     * 创建"已取消"结果
     */
    public static CommandResult cancelled(Command cmd) {
        return new CommandResult(
                cmd.id(),
                cmd.type(),
                ExecutionStatus.CANCELLED,
                null,
                null,
                System.currentTimeMillis()
        );
    }
}
