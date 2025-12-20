package com.nancheung.plugins.jetbrains.legadoreader.command.handler;

import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.CommandPayload;
import com.nancheung.plugins.jetbrains.legadoreader.event.CommandEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.EventPublisher;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.extern.slf4j.Slf4j;

/**
 * 切换阅读模式指令处理器
 * 切换行内阅读模式的显示/隐藏状态
 *
 * @author NanCheung
 */
@Slf4j
public class ToggleReadingModeHandler implements CommandHandler<CommandPayload> {

    @Override
    public CommandType supportedType() {
        return CommandType.TOGGLE_READING_MODE;
    }

    @Override
    public void handle(Command command) {
        EventPublisher publisher = EventPublisher.getInstance();
        PluginSettingsStorage settingsStorage = PluginSettingsStorage.getInstance();

        // 切换状态
        boolean newState = settingsStorage.toggleReadingMode();

        String message = newState ? "已开启行内阅读模式" : "已关闭行内阅读模式";

        publisher.publish(CommandEvent.completed(command, message));

        log.info("切换阅读模式: {}", message);
    }
}
