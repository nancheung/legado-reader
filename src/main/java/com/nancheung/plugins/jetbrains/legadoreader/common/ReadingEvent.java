package com.nancheung.plugins.jetbrains.legadoreader.common;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import org.jetbrains.annotations.Nullable;

/**
 * 阅读事件（不可变）
 * 携带阅读状态和数据的事件对象
 *
 * @param book            书籍信息
 * @param chapter         章节信息
 * @param content         章节内容（可能为 null）
 * @param chapterPosition 章节内光标位置
 * @param status          加载状态
 * @param direction       切换方向
 * @param error           错误信息（可能为 null）
 * @author NanCheung
 */
public record ReadingEvent(
        BookDTO book,
        BookChapterDTO chapter,
        @Nullable String content,
        int chapterPosition,
        Status status,
        Direction direction,
        @Nullable Throwable error
) {

    /**
     * 阅读事件状态
     */
    public enum Status {
        /**
         * 开始加载（切换章节时立即发送）
         */
        LOADING_STARTED,

        /**
         * 加载成功（数据已准备好）
         */
        LOADING_SUCCESS,

        /**
         * 加载失败（发生错误）
         */
        LOADING_FAILED
    }

    /**
     * 章节切换方向
     */
    public enum Direction {
        /**
         * 下一章
         */
        NEXT,

        /**
         * 上一章
         */
        PREVIOUS,

        /**
         * 跳转（从书架点击或目录跳转）
         */
        JUMP
    }

    /**
     * 创建"开始加载"事件
     * 章节切换时立即发送，用于让 UI 进入加载中状态
     *
     * @param book      书籍信息
     * @param chapter   章节信息
     * @param direction 切换方向
     * @return 加载开始事件
     */
    public static ReadingEvent loadingStarted(BookDTO book, BookChapterDTO chapter, Direction direction) {
        return new ReadingEvent(book, chapter, null, 0, Status.LOADING_STARTED, direction, null);
    }

    /**
     * 创建"加载成功"事件
     * 数据获取完成后发送，用于让 UI 显示内容
     *
     * @param book            书籍信息
     * @param chapter         章节信息
     * @param content         章节内容
     * @param chapterPosition 章节内光标位置
     * @param direction       切换方向
     * @return 加载成功事件
     */
    public static ReadingEvent loadingSuccess(BookDTO book, BookChapterDTO chapter, String content, int chapterPosition, Direction direction) {
        return new ReadingEvent(book, chapter, content, chapterPosition, Status.LOADING_SUCCESS, direction, null);
    }

    /**
     * 创建"加载失败"事件
     * 数据获取失败时发送，用于让 UI 显示错误提示
     *
     * @param book      书籍信息
     * @param chapter   章节信息
     * @param error     错误信息
     * @param direction 切换方向
     * @return 加载失败事件
     */
    public static ReadingEvent loadingFailed(BookDTO book, BookChapterDTO chapter, Throwable error, Direction direction) {
        return new ReadingEvent(book, chapter, null, 0, Status.LOADING_FAILED, direction, error);
    }
}