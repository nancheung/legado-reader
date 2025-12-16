package com.nancheung.plugins.jetbrains.legadoreader.command.handler;

import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.CommandPayload;
import com.nancheung.plugins.jetbrains.legadoreader.event.CommandEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.EventPublisher;
import com.nancheung.plugins.jetbrains.legadoreader.event.ReadingEvent;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSessionState;
import com.nancheung.plugins.jetbrains.legadoreader.service.PaginationManager;
import com.nancheung.plugins.jetbrains.legadoreader.service.ReadingSessionStateMachine;
import lombok.extern.slf4j.Slf4j;

/**
 * 返回书架指令处理器
 * 结束当前阅读会话，清空阅读状态
 *
 * @author NanCheung
 */
@Slf4j
public class BackToBookshelfHandler implements CommandHandler<CommandPayload> {

    @Override
    public CommandType supportedType() {
        return CommandType.BACK_TO_BOOKSHELF;
    }

    @Override
    public void handle(Command command) {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        ReadingSessionStateMachine stateMachine = ReadingSessionStateMachine.getInstance();
        PaginationManager paginationManager = PaginationManager.getInstance();
        EventPublisher publisher = EventPublisher.getInstance();

        log.info("返回书架，结束阅读会话");

        // 1. 发布会话结束事件
        publisher.publish(ReadingEvent.sessionEnded(command.id()));

        // 2. 清空阅读会话
        sessionManager.clear();

        // 3. 清空分页数据
        paginationManager.clear();

        // 4. 状态转换到空闲
        stateMachine.reset();

        // 5. 发布指令完成事件
        publisher.publish(CommandEvent.completed(command, "已返回书架"));

        log.debug("阅读会话已清空");
    }
}
