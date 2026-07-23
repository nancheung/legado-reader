package com.nancheung.plugins.jetbrains.legadoreader.command.handler;

import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.CommandPayload;
import com.nancheung.plugins.jetbrains.legadoreader.event.EventPublisher;
import com.nancheung.plugins.jetbrains.legadoreader.event.PaginationEvent;
import com.nancheung.plugins.jetbrains.legadoreader.service.IPaginationManager;
import com.nancheung.plugins.jetbrains.legadoreader.service.PaginationManager;
import com.nancheung.plugins.jetbrains.legadoreader.service.ProgressSyncService;
import lombok.extern.slf4j.Slf4j;

/**
 * 下一页指令处理器
 * 处理页内翻页，到达最后一页时自动触发下一章
 *
 * @author NanCheung
 */
@Slf4j
public class NextPageHandler implements CommandHandler<CommandPayload> {

    @Override
    public CommandType supportedType() {
        return CommandType.NEXT_PAGE;
    }

    @Override
    public void handle(Command command) {
        PaginationManager paginationManager = PaginationManager.getInstance();
        EventPublisher publisher = EventPublisher.getInstance();

        IPaginationManager.PageData currentPage = paginationManager.getCurrentPage();

        if (currentPage == null) {
            log.warn("当前没有分页数据");
            return;
        }

        int totalPages = paginationManager.getTotalPages();
        int currentPageIndex = currentPage.pageIndex();

        if (currentPageIndex < totalPages - 1) {
            // 页内翻页
            IPaginationManager.PageData nextPage = paginationManager.nextPage();

            if (nextPage != null) {
                publisher.publish(PaginationEvent.pageChanged(
                        nextPage.pageIndex() + 1,
                        totalPages,
                        nextPage.content()
                ));
                log.debug("翻到下一页: {}/{}", nextPage.pageIndex() + 1, totalPages);

                // 防抖同步页内阅读位置到服务器
                ProgressSyncService.getInstance().scheduleSyncCurrentPosition(nextPage.startPos());
            }

        } else {
            // 最后一页，触发下一章
            log.debug("已经是最后一页，切换到下一章");
            CommandBus.getInstance().dispatch(Command.of(CommandType.NEXT_CHAPTER));
        }
    }
}
