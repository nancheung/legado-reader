package com.nancheung.plugins.jetbrains.legadoreader.command.handler;

import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.CommandPayload;

/**
 * 指令处理器接口
 * 每个指令类型对应一个处理器实现
 *
 * @param <P> 指令参数类型
 * @author NanCheung
 */
public interface CommandHandler<P extends CommandPayload> {

    /**
     * 支持的指令类型
     *
     * @return 指令类型
     */
    CommandType supportedType();

    /**
     * 处理指令
     * 注意：处理器内部负责：
     * 1. 调用数据层获取数据
     * 2. 发布相应的事件（loading/success/failed）
     * 3. 更新会话状态
     *
     * @param command 指令对象
     */
    void handle(Command command);

    /**
     * 是否可以处理该指令（前置条件检查）
     * 默认实现：检查指令类型是否匹配
     *
     * @param command 指令对象
     * @return true 如果可以处理
     */
    default boolean canHandle(Command command) {
        return command.type() == supportedType();
    }
}
