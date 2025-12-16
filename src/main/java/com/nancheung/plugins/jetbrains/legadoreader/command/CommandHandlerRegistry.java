package com.nancheung.plugins.jetbrains.legadoreader.command;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.command.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指令处理器注册表（Application Service）
 * 根据指令类型路由到对应的处理器
 *
 * @author NanCheung
 */
@Slf4j
@Service
public final class CommandHandlerRegistry {

    private final Map<CommandType, CommandHandler<?>> handlers = new ConcurrentHashMap<>();

    /**
     * 获取单例实例
     */
    public static CommandHandlerRegistry getInstance() {
        return ApplicationManager.getApplication().getService(CommandHandlerRegistry.class);
    }

    /**
     * 注册处理器
     *
     * @param handler 处理器实例
     */
    public void register(CommandHandler<?> handler) {
        CommandType type = handler.supportedType();
        if (handlers.containsKey(type)) {
            log.warn("处理器已存在，将被覆盖: {}", type);
        }
        handlers.put(type, handler);
        log.info("注册指令处理器: {}", type);
    }

    /**
     * 获取处理器
     *
     * @param type 指令类型
     * @return 处理器（Optional）
     */
    public Optional<CommandHandler<?>> getHandler(CommandType type) {
        return Optional.ofNullable(handlers.get(type));
    }

    /**
     * 移除处理器
     *
     * @param type 指令类型
     */
    public void unregister(CommandType type) {
        handlers.remove(type);
        log.info("移除指令处理器: {}", type);
    }

    /**
     * 清空所有处理器
     */
    public void clear() {
        handlers.clear();
        log.info("清空所有指令处理器");
    }

    /**
     * 获取已注册的处理器数量
     */
    public int size() {
        return handlers.size();
    }
}
