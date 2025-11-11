package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderFactory;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
import com.nancheung.plugins.jetbrains.legadoreader.service.EditorLineSchedulerService;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 编辑器行内阅读鼠标监听器
 * 单击 → 下一页
 * 双击 → 上一页
 */
public class SwitchLineMouseListener implements EditorMouseListener {
    private static final Logger LOG = Logger.getInstance(SwitchLineMouseListener.class);

    /**
     * 双击延迟判断时间（毫秒）
     */
    private static final long DOUBLE_CLICK_DELAY_MS = 200;

    /**
     * 行内阅读服务
     */
    private static final IReader readerAction = ReaderFactory.EDITOR_LINE.getReader();

    /**
     * 双击标志（使用 AtomicBoolean 保证线程安全）
     */
    private final AtomicBoolean isDoubleClick = new AtomicBoolean(false);

    @Override
    public void mouseClicked(@NotNull EditorMouseEvent e) {
        try {
            // 判断是否启用了行内阅读
            if (!PluginSettingsManager.getInstance().isEnableShowBodyInLine()) {
                return;
            }

            // 如果是双击事件，设置标志并返回
            if (e.getMouseEvent().getClickCount() > 1) {
                isDoubleClick.set(true);
                LOG.debug("检测到双击事件");
                return;
            }

            // 延迟判断是双击还是单击
            EditorLineSchedulerService.getInstance().schedule(() -> {
                try {
                    if (isDoubleClick.getAndSet(false)) {
                        // 双击 → 上一页
                        handleDoubleClick(e);
                    } else {
                        // 单击 → 下一页
                        handleSingleClick(e);
                    }
                } catch (Exception ex) {
                    LOG.error("处理鼠标点击事件失败", ex);
                }
            }, DOUBLE_CLICK_DELAY_MS, TimeUnit.MILLISECONDS);

        } catch (Exception ex) {
            LOG.error("鼠标点击事件处理异常", ex);
        }
    }

    /**
     * 处理单击事件 → 下一页
     *
     * @param e 鼠标事件
     */
    private void handleSingleClick(EditorMouseEvent e) {
        try {
            LOG.debug("单击 → 下一页");
            readerAction.nextPage();
        } catch (Exception ex) {
            LOG.error("执行下一页失败", ex);
        }
    }

    /**
     * 处理双击事件 → 上一页
     *
     * @param e 鼠标事件
     */
    private void handleDoubleClick(EditorMouseEvent e) {
        try {
            LOG.debug("双击 → 上一页");
            readerAction.previousPage();
        } catch (Exception ex) {
            LOG.error("执行上一页失败", ex);
        }
    }
}