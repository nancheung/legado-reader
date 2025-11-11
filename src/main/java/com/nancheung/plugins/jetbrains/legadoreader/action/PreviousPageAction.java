package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderGlobalFacade;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 上一页
 * 支持 ToolWindow 和 EditorLine 双模式
 */
@Slf4j
public class PreviousPageAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
            ReaderGlobalFacade.getInstance().previousPage();
            log.debug("翻到上一页");
    }
}
