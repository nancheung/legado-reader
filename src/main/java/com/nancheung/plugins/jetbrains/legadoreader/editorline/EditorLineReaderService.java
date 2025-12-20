package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.nancheung.plugins.jetbrains.legadoreader.event.PaginationEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.ReaderEvent;
import com.nancheung.plugins.jetbrains.legadoreader.event.ReaderEventListener;
import com.nancheung.plugins.jetbrains.legadoreader.event.ReadingEvent;
import com.nancheung.plugins.jetbrains.legadoreader.service.PaginationManager;
import com.nancheung.plugins.jetbrains.legadoreader.storage.PluginSettingsStorage;
import lombok.extern.slf4j.Slf4j;

/**
 * 编辑器行内阅读服务，请使用事件订阅模式，不要直接调用此类的方法
 * 实现 IReader 接口（保持兼容），但实际逻辑已迁移到事件订阅模式
 * 订阅 ReaderEventListener，监听章节和分页事件以触发编辑器刷新
 *
 * @author NanCheung
 */
@Slf4j
public class EditorLineReaderService {

    private final PaginationManager paginationManager;

    /**
     * 构造函数
     * 订阅阅读事件，当章节切换或分页时自动刷新编辑器
     */
    public EditorLineReaderService() {
        this.paginationManager = PaginationManager.getInstance();

        // 订阅事件
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect()
                .subscribe(ReaderEventListener.TOPIC, (ReaderEventListener) EditorLineReaderService.this::onEvent);

        log.debug("EditorLineReaderService 已初始化");
    }

    /**
     * 处理事件
     * 根据事件类型执行不同的操作
     *
     * @param event 事件对象
     */
    private void onEvent(ReaderEvent event) {
        switch (event) {
            case ReadingEvent e -> onReadingEvent(e);
            case PaginationEvent e -> onPaginationEvent(e);
            default -> {
                // 忽略其他事件类型
            }
        }
    }

    /**
     * 处理阅读事件
     * 当章节加载成功时，重新分页并定位页码
     *
     * @param event 阅读事件
     */
    private void onReadingEvent(ReadingEvent event) {
        if (event.type() == ReadingEvent.ReadingEventType.CHAPTER_LOADED) {
            // 获取内容并重新分页
            String content = event.content();
            int pageSize = PluginSettingsStorage.getInstance().getState().textBodyFontSize != null
                    ? PluginSettingsStorage.getInstance().getState().textBodyFontSize * 2
                    : 30;

            paginationManager.paginate(content, pageSize);

            // 根据方向定位页码
            if (event.direction() == ReadingEvent.Direction.PREVIOUS) {
                // 上一章，定位到最后一页
                paginationManager.goToLastPage();
                log.debug("上一章，定位到最后一页");
            } else {
                // 下一章或跳转，定位到第一页
                paginationManager.goToFirstPage();
                log.debug("下一章或跳转，定位到第一页");
            }

            // 刷新编辑器
            refreshEditor();

            log.info("EditorLine 事件处理完成：{}", event.chapter().getTitle());
        }
    }

    /**
     * 处理分页事件
     * 当分页变更时，刷新编辑器显示
     *
     * @param event 分页事件
     */
    private void onPaginationEvent(PaginationEvent event) {
        // 刷新编辑器显示新的页码
        refreshEditor();
        log.debug("分页事件：页码 {}/{}", event.currentPage(), event.totalPages());
    }

    /**
     * 刷新编辑器，触发行内内容重绘
     * 在 EDT 线程中执行，确保线程安全
     */
    private void refreshEditor() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // 获取当前打开的项目
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

            for (Project project : openProjects) {
                if (project != null && !project.isDisposed()) {
                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    if (editor != null && !editor.isDisposed()) {
                        // 触发编辑器内容组件重绘
                        editor.getContentComponent().repaint();
                        log.debug("触发编辑器重绘");
                    }
                }
            }
        });
    }
}
