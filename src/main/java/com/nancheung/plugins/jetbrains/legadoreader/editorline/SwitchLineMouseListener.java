package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.nancheung.plugins.jetbrains.legadoreader.action.BodyInLineData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SwitchLineMouseListener implements EditorMouseListener, Disposable {
    private final ScheduledExecutorService clickScheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean isDoubleClick = false;
    
    @Override
    public void mouseClicked(@NotNull EditorMouseEvent e) {
        // 判断是否启用了行内阅读
        if (!BodyInLineData.isEnableShowBodyInLine()) {
            return;
        }
        
        // 如果是双击事件，设置标志并返回
        if (e.getMouseEvent().getClickCount() > 1) {
            isDoubleClick = true;
            return;
        }
        
        // 延迟200ms后判断是双击还是单击
        clickScheduler.schedule(() -> {
            if (isDoubleClick) {
                handleDoubleClick(e);
            } else {
                handleSingleClick(e);
            }
            isDoubleClick = false;
        }, 200, TimeUnit.MILLISECONDS);
    }
    
    private void handleSingleClick(EditorMouseEvent e) {
        System.out.println("单击下一行");
        BodyInLineData.nextLine();
    }
    
    private void handleDoubleClick(EditorMouseEvent e) {
        System.out.println("双击上一行");
        BodyInLineData.previousLine();
    }
    
    @Override
    public void dispose() {
        // 关闭线程池
        clickScheduler.shutdown();
    }
}