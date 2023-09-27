package com.nancheung.plugins.jetbrains.legadoreader.action;

import cn.hutool.core.collection.CollUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;
import org.jetbrains.annotations.NotNull;

/**
 * 自动更新显示当前阅读的书籍信息
 *
 * @author NanCheung
 */
public class ShowBookInfoAction extends AnAction {

    private String bookName = "";
    private String chapterTitle = "";

    @Override
    public void actionPerformed(AnActionEvent e) {
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        BookDTO book = CurrentReadData.getBook();

        if (book == null || CollUtil.isEmpty(CurrentReadData.getBookChapterList())) {
            return;
        }

        String name = book.getName();
        String title = CurrentReadData.getBookChapter().getTitle();

        if (bookName.equals(name) && chapterTitle.equals(title)) {
            return;
        }

        bookName = name;
        chapterTitle = title;

        // 将阅读信息设置到action的描述中
        String text = name + " - " + title;
        e.getPresentation().setText(text);
    }
}
