package com.nancheung.plugins.jetbrains.legadoreader.command;

import com.nancheung.plugins.jetbrains.legadoreader.command.payload.CommandPayload;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 用户指令（不可变）
 * 封装用户发起的操作及其参数
 *
 * @param id        指令唯一 ID（用于追踪和关联事件）
 * @param type      指令类型
 * @param payload   指令参数（可能为 null）
 * @param timestamp 指令发起时间戳
 * @author NanCheung
 */
public record Command(
        String id,
        CommandType type,
        @Nullable CommandPayload payload,
        long timestamp
) {
    /**
     * 创建无参数指令（自动生成 ID 和时间戳）
     *
     * @param type 指令类型
     * @return 指令对象
     */
    public static Command of(CommandType type) {
        return new Command(
                UUID.randomUUID().toString(),
                type,
                null,
                System.currentTimeMillis()
        );
    }

    /**
     * 创建带参数指令（自动生成 ID 和时间戳）
     *
     * @param type    指令类型
     * @param payload 指令参数
     * @return 指令对象
     */
    public static Command of(CommandType type, CommandPayload payload) {
        return new Command(
                UUID.randomUUID().toString(),
                type,
                payload,
                System.currentTimeMillis()
        );
    }
}
