package com.nancheung.plugins.jetbrains.legadoreader.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandBus;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.SelectBookPayload;
import lombok.extern.slf4j.Slf4j;

/**
 * 阅读全局门面（兼容层）
 * <p>
 * 重构说明：
 * - 从 2.0 版本开始，所有方法内部改为调用 CommandBus
 * - 保留现有 API 不变，确保向后兼容
 * - 所有方法标记为 @Deprecated，建议使用新的 CommandBus API
 * - 未来版本将移除此兼容层
 * <p>
 * 迁移指南：
 * - 旧代码：ReaderGlobalFacade.getInstance().nextChapter()
 * - 新代码：CommandBus.getInstance().dispatch(Command.of(CommandType.NEXT_CHAPTER))
 *
 * @author NanCheung
 * @deprecated 请使用 {@link CommandBus} 和 {@link Command}
 */
@Slf4j
@Service
@Deprecated(since = "2.0", forRemoval = true)
public final class ReaderGlobalFacade implements IReader {

    private final CommandBus commandBus;

    /**
     * 构造函数（由 IntelliJ Platform 调用）
     */
    public ReaderGlobalFacade() {
        this.commandBus = CommandBus.getInstance();
        log.debug("ReaderGlobalFacade 已初始化（兼容层）");
    }

    /**
     * 获取服务实例
     *
     * @return 服务实例
     * @deprecated 请使用 {@link CommandBus#getInstance()}
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static ReaderGlobalFacade getInstance() {
        return ApplicationManager.getApplication().getService(ReaderGlobalFacade.class);
    }

    /**
     * 上一页
     * <p>
     * 兼容方法，内部调用 CommandBus
     *
     * @deprecated 请使用 CommandBus.getInstance().dispatch(Command.of(CommandType.PREVIOUS_PAGE))
     */
    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void previousPage() {
        commandBus.dispatchAsync(Command.of(CommandType.PREVIOUS_PAGE));
    }

    /**
     * 下一页
     * <p>
     * 兼容方法，内部调用 CommandBus
     *
     * @deprecated 请使用 CommandBus.getInstance().dispatch(Command.of(CommandType.NEXT_PAGE))
     */
    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void nextPage() {
        commandBus.dispatchAsync(Command.of(CommandType.NEXT_PAGE));
    }

    /**
     * 上一章
     * <p>
     * 兼容方法，内部调用 CommandBus
     *
     * @deprecated 请使用 CommandBus.getInstance().dispatch(Command.of(CommandType.PREVIOUS_CHAPTER))
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public void previousChapter() {
        commandBus.dispatchAsync(Command.of(CommandType.PREVIOUS_CHAPTER));
    }

    /**
     * 下一章
     * <p>
     * 兼容方法，内部调用 CommandBus
     *
     * @deprecated 请使用 CommandBus.getInstance().dispatch(Command.of(CommandType.NEXT_CHAPTER))
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public void nextChapter() {
        commandBus.dispatchAsync(Command.of(CommandType.NEXT_CHAPTER));
    }

    /**
     * 加载章节（统一入口）
     * 适用于：首次打开书籍、从目录跳转到指定章节
     * <p>
     * 兼容方法，内部调用 CommandBus
     *
     * @param book         书籍信息
     * @param chapterIndex 目标章节索引
     * @deprecated 请使用 CommandBus.getInstance().dispatch(Command.of(CommandType.SELECT_BOOK, new SelectBookPayload(book, chapterIndex)))
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public void loadChapter(BookDTO book, int chapterIndex) {
        commandBus.dispatchAsync(Command.of(
                CommandType.SELECT_BOOK,
                new SelectBookPayload(book, chapterIndex)
        ));
    }

    /**
     * 分页（统一入口）
     * <p>
     * 注意：此方法已废弃，分页现在由 PaginationManager 统一管理
     * 不再广播到各个 Reader，而是由事件订阅者响应 PaginationEvent
     *
     * @param chapterContent 章节内容
     * @param pageSize       每页大小
     * @deprecated 请使用 PaginationManager.getInstance().paginate(content, pageSize)
     */
    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void splitChapter(String chapterContent, int pageSize) {
        // 此方法已废弃，不再实现
        log.warn("splitChapter() 方法已废弃，请使用 PaginationManager.getInstance().paginate()");
    }
}
