package com.nancheung.plugins.jetbrains.legadoreader.command.handler;

import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.CommandPayload;
import com.nancheung.plugins.jetbrains.legadoreader.event.CommandEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.EventPublisher;
import com.nancheung.plugins.jetbrains.legadoreader.event.PaginationEvent;
import com.nancheung.plugins.jetbrains.legadoreader.service.IPaginationManager;
import com.nancheung.plugins.jetbrains.legadoreader.service.PaginationManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 上一页指令处理器
 * 处理页内翻页，到达第一页时自动触发上一章
 *
 * @author NanCheung
 */
@Slf4j
public class PreviousPageHandler implements CommandHandler<CommandPayload> {

    @Override
    public CommandType supportedType() {
        return CommandType.PREVIOUS_PAGE;
    }

    @Override
    public void handle(Command command) {
        PaginationManager paginationManager = PaginationManager.getInstance();
        EventPublisher publisher = EventPublisher.getInstance();

        IPaginationManager.PageData currentPage = paginationManager.getCurrentPage();

        if (currentPage == null) {
            publisher.publish(CommandEvent.failed(command, "当前没有分页数据"));
            return;
        }

        int totalPages = paginationManager.getTotalPages();
        int currentPageIndex = currentPage.pageIndex();

        if (currentPageIndex > 0) {
            // 页内翻页
            IPaginationManager.PageData prevPage = paginationManager.previousPage();

            if (prevPage != null) {
                publisher.publish(PaginationEvent.pageChanged(
                        prevPage.pageIndex() + 1,
                        totalPages,
                        prevPage.content()
                ));
                publisher.publish(CommandEvent.completed(command, null));
                log.debug("翻到上一页: {}/{}", prevPage.pageIndex() + 1, totalPages);
            }

        } else {
            // 第一页，触发上一章
            log.debug("已经是第一页，切换到上一章");
            CommandBus.getInstance().dispatch(Command.of(CommandType.PREVIOUS_CHAPTER));
            publisher.publish(CommandEvent.completed(command, null));
        }
    }
}
