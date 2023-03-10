package com.nancheung.plugins.jetbrains.legadoreader.action;

import cn.hutool.core.collection.CollUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;
import org.jetbrains.annotations.NotNull;

public class ShowBookInfoAction extends AnAction {
    
    @Override
    public void actionPerformed(AnActionEvent e) {
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        BookDTO book = CurrentReadData.getBook();
        
        if (book == null || CollUtil.isEmpty(CurrentReadData.getBookChapterList())) {
            return;
        }
        
        // 将阅读信息设置到action的描述中
        String text = book.getName() + " - " + CurrentReadData.getBookChapter().getTitle();
        e.getPresentation().setText(text);
    }
}
