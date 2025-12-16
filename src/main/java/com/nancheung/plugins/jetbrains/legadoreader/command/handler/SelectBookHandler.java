package com.nancheung.plugins.jetbrains.legadoreader.command.handler;

import com.nancheung.plugins.jetbrains.legadoreader.api.ApiUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.command.Command;
import com.nancheung.plugins.jetbrains.legadoreader.command.CommandType;
import com.nancheung.plugins.jetbrains.legadoreader.command.payload.SelectBookPayload;
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
 * 选择书籍指令处理器
 * 处理从书架选择书籍、跳转到指定章节的操作
 *
 * @author NanCheung
 */
@Slf4j
public class SelectBookHandler implements CommandHandler<SelectBookPayload> {

    @Override
    public CommandType supportedType() {
        return CommandType.SELECT_BOOK;
    }

    @Override
    public void handle(Command command) {
        EventPublisher publisher = EventPublisher.getInstance();
        ReadingSessionStateMachine stateMachine = ReadingSessionStateMachine.getInstance();

        // 1. 获取参数
        if (!(command.payload() instanceof SelectBookPayload payload)) {
            publisher.publish(CommandEvent.failed(command, "参数类型错误"));
            return;
        }

        BookDTO book = payload.book();
        int chapterIndex = payload.chapterIndex();

        log.info("加载章节: book={}, chapterIndex={}", book.getName(), chapterIndex);

        // 2. 检查当前状态，防止重复加载
        if (stateMachine.isLoading()) {
            log.warn("当前正在加载中，忽略新的加载请求");
            publisher.publish(CommandEvent.failed(command, "当前正在加载中，请稍后再试"));
            return;
        }

        // 3. 状态转换到加载中
        if (!stateMachine.transition(ReadingSessionState.LOADING)) {
            publisher.publish(CommandEvent.failed(command, "当前状态不允许加载章节"));
            return;
        }

        // 4. 创建临时章节对象，发布加载开始事件
        BookChapterDTO tempChapter = new BookChapterDTO();
        tempChapter.setIndex(chapterIndex);
        publisher.publish(ReadingEvent.chapterLoading(
                command.id(),
                book,
                tempChapter,
                ReadingEvent.Direction.JUMP
        ));

        // 5. 异步获取章节列表和内容
        CompletableFuture.runAsync(() -> {
            try {
                // 获取章节列表
                List<BookChapterDTO> chapters = ApiUtil.getChapterList(book.getBookUrl());

                // 边界检查
                if (chapterIndex < 0 || chapterIndex >= chapters.size()) {
                    throw new IllegalArgumentException("章节索引越界: " + chapterIndex);
                }

                BookChapterDTO chapter = chapters.get(chapterIndex);

                // 获取章节内容
                String content = ApiUtil.getBookContent(book.getBookUrl(), chapterIndex);

                // 创建并设置会话
                ReadingSession session = new ReadingSession(book, chapters, chapterIndex, content);
                ReadingSessionManager.getInstance().setSession(session);

                // 状态转换到阅读中
                stateMachine.transition(ReadingSessionState.READING);

                // 发布加载成功事件
                int position = (chapterIndex == book.getDurChapterIndex()) ? book.getDurChapterPos() : 0;
                publisher.publish(ReadingEvent.chapterLoaded(
                        command.id(),
                        book,
                        chapter,
                        content,
                        position,
                        ReadingEvent.Direction.JUMP
                ));

                publisher.publish(CommandEvent.completed(command, "打开: " + book.getName()));

                log.info("章节加载成功: {}", chapter.getTitle());

                // 异步同步进度
                syncProgressAsync(book, chapterIndex, chapter.getTitle(), position);

            } catch (Exception e) {
                // 状态转换到错误
                stateMachine.transition(ReadingSessionState.ERROR);

                // 发布加载失败事件
                BookChapterDTO failedChapter = new BookChapterDTO();
                failedChapter.setIndex(chapterIndex);
                publisher.publish(ReadingEvent.chapterLoadFailed(
                        command.id(),
                        book,
                        failedChapter,
                        e,
                        ReadingEvent.Direction.JUMP
                ));

                publisher.publish(CommandEvent.failed(command, e.getMessage()));

                if (Boolean.TRUE.equals(PluginSettingsStorage.getInstance().getState().enableErrorLog)) {
                    log.error("章节加载失败", e);
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
