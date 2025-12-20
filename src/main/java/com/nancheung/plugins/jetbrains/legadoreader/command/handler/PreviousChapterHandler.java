package com.nancheung.plugins.jetbrains.legadoreader.command.handler;

import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.CommandPayload;
import com.nancheung.plugins.jetbrains.legadoreader.event.CommandEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.EventPublisher;
import com.nancheung.plugins.jetbrains.legadoreader.event.ReadingEvent;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSessionState;
import com.nancheung.plugins.jetbrains.legadoreader.service.ReadingSessionStateMachine;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 上一章指令处理器
 *
 * @author NanCheung
 */
@Slf4j
public class PreviousChapterHandler implements CommandHandler<CommandPayload> {

    @Override
    public CommandType supportedType() {
        return CommandType.PREVIOUS_CHAPTER;
    }

    @Override
    public void handle(Command command) {
        ReadingSessionManager sessionManager = ReadingSessionManager.getInstance();
        ReadingSessionStateMachine stateMachine = ReadingSessionStateMachine.getInstance();
        EventPublisher publisher = EventPublisher.getInstance();

        ReadingSession session = sessionManager.getSession();

        // 1. 前置检查
        if (session == null) {
            publisher.publish(CommandEvent.failed(command, "没有当前阅读会话"));
            return;
        }

        int currentIndex = sessionManager.getCurrentChapterIndex();

        if (currentIndex <= 0) {
            publisher.publish(CommandEvent.failed(command, "已经是第一章"));
            return;
        }

        // 2. 检查当前状态，防止重复加载
        if (stateMachine.isLoading()) {
            log.warn("当前正在加载中，忽略切换章节请求");
            publisher.publish(CommandEvent.failed(command, "当前正在加载中，请稍后再试"));
            return;
        }

        // 3. 状态转换
        if (!stateMachine.transition(ReadingSessionState.LOADING)) {
            publisher.publish(CommandEvent.failed(command, "当前状态不允许切换章节"));
            return;
        }

        // 4. 准备数据
        int prevIndex = currentIndex - 1;
        BookDTO book = session.book();
        BookChapterDTO tempChapter = new BookChapterDTO();
        tempChapter.setIndex(prevIndex);

        log.info("开始切换到上一章: {} -> {}", currentIndex, prevIndex);

        // 5. 发布加载开始事件
        publisher.publish(ReadingEvent.chapterLoading(
                command.id(),
                book,
                tempChapter,
                ReadingEvent.Direction.PREVIOUS
        ));

        // 6. 异步加载数据
        CompletableFuture.runAsync(() -> {
            try {
                List<BookChapterDTO> chapters = sessionManager.getChapters();
                BookChapterDTO chapter = chapters.get(prevIndex);
                String content = ApiUtil.getBookContent(book.getBookUrl(), prevIndex);

                // 更新会话
                sessionManager.previousChapter();
                sessionManager.setContent(content);

                // 状态转换
                stateMachine.transition(ReadingSessionState.READING);

                // 发布成功事件
                publisher.publish(ReadingEvent.chapterLoaded(
                        command.id(),
                        book,
                        chapter,
                        content,
                        0,
                        ReadingEvent.Direction.PREVIOUS
                ));

                publisher.publish(CommandEvent.completed(command, "切换到: " + chapter.getTitle()));

                log.info("切换到上一章成功：{}", chapter.getTitle());

                // 异步同步进度
                syncProgressAsync(book, prevIndex, chapter.getTitle(), 0);

            } catch (Exception e) {
                // 回滚状态
                sessionManager.nextChapter();
                stateMachine.transition(ReadingSessionState.READING);

                publisher.publish(ReadingEvent.chapterLoadFailed(
                        command.id(),
                        book,
                        tempChapter,
                        e,
                        ReadingEvent.Direction.PREVIOUS
                ));

                publisher.publish(CommandEvent.failed(command, e.getMessage()));

                if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                    log.error("切换到上一章失败", e);
                }
            }
        });
    }

    private void syncProgressAsync(BookDTO book, int chapterIndex, String chapterTitle, int position) {
        CompletableFuture.runAsync(() -> {
            try {
                ApiUtil.saveBookProgress(book.getAuthor(), book.getName(), chapterIndex, chapterTitle, position);
                log.debug("同步阅读进度成功：{} - {}", book.getName(), chapterTitle);
            } catch (Exception e) {
                log.warn("同步阅读进度失败", e);
            }
        });
    }
}
