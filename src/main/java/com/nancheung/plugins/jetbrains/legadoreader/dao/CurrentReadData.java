package com.nancheung.plugins.jetbrains.legadoreader.dao;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSession;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 当前阅读数据访问类（兼容层）
 * 代理到 ReadingSessionManager，保持向后兼容
 *
 * @author NanCheung
 * @deprecated 建议直接使用 {@link ReadingSessionManager}
 */
@UtilityClass
@Deprecated
public class CurrentReadData {

    /**
     * 获取当前书籍
     *
     * @return 当前书籍
     * @deprecated 使用 {@link ReadingSessionManager#getCurrentBook()}
     */
    @Deprecated
    public BookDTO getBook() {
        return ReadingSessionManager.getInstance().getCurrentBook();
    }

    /**
     * 设置当前书籍
     *
     * @param book 书籍
     * @deprecated 使用 {@link ReadingSessionManager#setSession(ReadingSession)}
     */
    @Deprecated
    public void setBook(BookDTO book) {
        ReadingSession current = ReadingSessionManager.getInstance().getSession();
        ReadingSession newSession = new ReadingSession(
                book,
                current != null ? current.chapters() : null,
                current != null ? current.currentChapterIndex() : 0,
                null
        );
        ReadingSessionManager.getInstance().setSession(newSession);
    }

    /**
     * 获取章节列表
     *
     * @return 章节列表
     * @deprecated 使用 {@link ReadingSessionManager#getChapters()}
     */
    @Deprecated
    public List<BookChapterDTO> getBookChapterList() {
        return ReadingSessionManager.getInstance().getChapters();
    }

    /**
     * 设置章节列表
     *
     * @param chapters 章节列表
     * @deprecated 使用 {@link ReadingSessionManager#setSession(ReadingSession)}
     */
    @Deprecated
    public void setBookChapterList(List<BookChapterDTO> chapters) {
        ReadingSession current = ReadingSessionManager.getInstance().getSession();
        if (current != null) {
            ReadingSession newSession = new ReadingSession(
                    current.book(),
                    chapters,
                    current.currentChapterIndex(),
                    null
            );
            ReadingSessionManager.getInstance().setSession(newSession);
        } else {
            // 如果没有当前会话，创建一个新的
            ReadingSession newSession = new ReadingSession(null, chapters, 0, null);
            ReadingSessionManager.getInstance().setSession(newSession);
        }
    }

    /**
     * 获取当前章节索引
     *
     * @return 章节索引
     * @deprecated 使用 {@link ReadingSessionManager#getCurrentChapterIndex()}
     */
    @Deprecated
    public int getBookIndex() {
        return ReadingSessionManager.getInstance().getCurrentChapterIndex();
    }

    /**
     * 设置当前章节索引
     *
     * @param index 章节索引
     * @deprecated 使用 {@link ReadingSessionManager#setChapterIndex(int)}
     */
    @Deprecated
    public void setBookIndex(int index) {
        ReadingSessionManager.getInstance().setChapterIndex(index);
    }

    /**
     * 章节索引原子递增
     * （方法名保留 Atomic 以保持兼容，但现在真正是线程安全的）
     *
     * @deprecated 使用 {@link ReadingSessionManager#nextChapter()}
     */
    @Deprecated
    public void indexAtomicIncrement() {
        ReadingSessionManager.getInstance().nextChapter();
    }

    /**
     * 章节索引原子递减
     * （方法名保留 Atomic 以保持兼容，但现在真正是线程安全的）
     *
     * @deprecated 使用 {@link ReadingSessionManager#previousChapter()}
     */
    @Deprecated
    public void indexAtomicDecrement() {
        ReadingSessionManager.getInstance().previousChapter();
    }

    /**
     * 获取当前章节
     *
     * @return 当前章节
     * @deprecated 使用 {@link ReadingSessionManager#getCurrentChapter()}
     */
    @Deprecated
    public BookChapterDTO getBookChapter() {
        return ReadingSessionManager.getInstance().getCurrentChapter();
    }

    /**
     * 获取正文内容
     *
     * @return 正文内容
     * @deprecated 使用 {@link ReadingSessionManager#getCurrentContent()}
     */
    @Deprecated
    public String getBodyContent() {
        return ReadingSessionManager.getInstance().getCurrentContent();
    }

    /**
     * 设置正文内容
     *
     * @param content 正文内容
     * @deprecated 使用 {@link ReadingSessionManager#setContent(String)}
     */
    @Deprecated
    public void setBodyContent(String content) {
        ReadingSessionManager.getInstance().setContent(content);
    }
}
