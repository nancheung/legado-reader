package com.nancheung.plugins.jetbrains.legadoreader.presentation.common;

import com.intellij.openapi.application.ApplicationManager;
import com.nancheung.plugins.jetbrains.legadoreader.event.*;
import lombok.extern.slf4j.Slf4j;

/**
 * UI 事件订阅器基类
 * 提供事件分发和 EDT 线程保证
 * 所有 UI 组件都应该继承此类并订阅事件
 * 必须在子类构造函数中调用 super() 来激活订阅
 * 需要消费什么事件就重写对应的方法
 *
 * @author NanCheung
 */
@Slf4j
public abstract class UIEventSubscriber implements ReaderEventListener {

    /**
     * 构造函数中自动订阅事件
     * 子类需要调用 super() 来激活订阅
     */
    protected UIEventSubscriber() {
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect()
                .subscribe(ReaderEventListener.TOPIC, this);

        log.debug("UI 订阅器已注册: {}", this.getClass().getSimpleName());
    }

    /**
     * 处理事件（final，不可重写）
     * 确保所有事件处理都在 EDT 线程中执行
     *
     * @param event 事件对象
     */
    @Override
    public final void onEvent(ReaderEvent event) {
        // 确保在 EDT 线程中执行 UI 更新
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                dispatchEvent(event);
            } catch (Exception e) {
                log.error("UI 事件处理失败: {}", event.getClass().getSimpleName(), e);
            }
        });
    }

    /**
     * 分发事件到具体处理方法
     *
     * @param event 事件对象
     */
    private void dispatchEvent(ReaderEvent event) {
        switch (event) {
            case BookshelfEvent e -> onBookshelfEvent(e);
            case ReadingEvent e -> onReadingEvent(e);
            case PaginationEvent e -> onPaginationEvent(e);
            case SettingsChangedEvent e -> onSettingsChangedEvent(e);
            case ProgressSyncEvent e -> onProgressSyncEvent(e);
        }
    }

    /**
     * 处理书架事件
     * 用于显示书架列表、加载状态等
     *
     * @param event 书架事件
     */
    protected void onBookshelfEvent(BookshelfEvent event) {
        // 子类可选择性重写
    }

    /**
     * 处理阅读内容事件
     * 用于显示章节内容、加载状态、错误提示等
     *
     * @param event 阅读事件
     */
    protected void onReadingEvent(ReadingEvent event) {
        // 子类可选择性重写
    }

    /**
     * 处理分页事件
     * 用于更新当前页显示
     *
     * @param event 分页事件
     */
    protected void onPaginationEvent(PaginationEvent event) {
        // 子类可选择性重写
    }

    /**
     * 处理设置变更事件
     * 用于响应用户在设置页面的修改
     *
     * @param event 设置变更事件
     */
    protected void onSettingsChangedEvent(SettingsChangedEvent event) {
        // 子类可选择性重写
    }

    /**
     * 处理进度同步事件
     * 用于展示同步状态提示
     *
     * @param event 进度同步事件
     */
    protected void onProgressSyncEvent(ProgressSyncEvent event) {
        // 子类可选择性重写
    }
}
