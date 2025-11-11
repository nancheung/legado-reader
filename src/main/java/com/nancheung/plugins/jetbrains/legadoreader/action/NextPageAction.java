package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderGlobalFacade;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 下一页
 * 支持 ToolWindow 和 EditorLine 双模式
 */
@Slf4j
public class NextPageAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ReaderGlobalFacade.getInstance().nextPage();
        log.debug("翻到下一页");
    }
}
