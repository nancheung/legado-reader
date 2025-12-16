package com.nancheung.plugins.jetbrains.legadoreader.event;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 事件发布器（Application Service）
 * 负责将事件发布到 MessageBus
 *
 * @author NanCheung
 */
@Slf4j
@Service
public final class EventPublisher {

    /**
     * 获取单例实例
     */
    public static EventPublisher getInstance() {
        return ApplicationManager.getApplication().getService(EventPublisher.class);
    }

    /**
     * 发布事件（同步）
     * 事件会立即广播给所有订阅者
     *
     * @param event 事件对象
     */
    public void publish(ReaderEvent event) {
        ApplicationManager.getApplication()
                .getMessageBus()
                .syncPublisher(ReaderEventListener.TOPIC)
                .onEvent(event);

        log.debug("事件已发布: type={}, eventId={}", event.getClass().getSimpleName(), event.eventId());
    }

    /**
     * 异步发布事件
     * 事件将在后台线程中发布
     *
     * @param event 事件对象
     */
    public void publishAsync(ReaderEvent event) {
        CompletableFuture.runAsync(() -> publish(event));
    }
}
