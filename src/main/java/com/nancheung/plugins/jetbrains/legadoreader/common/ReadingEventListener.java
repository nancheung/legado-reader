package com.nancheung.plugins.jetbrains.legadoreader.common;

import com.intellij.util.messages.Topic;

/**
 * 阅读事件监听器
 * 通过 MessageBus 订阅阅读相关的事件
 *
 * @author NanCheung
 */
public interface ReadingEventListener {

    /**
     * Application 级别的 Topic
     * 在多个 IDE 窗口间共享事件
     */
    @Topic.AppLevel
    Topic<ReadingEventListener> TOPIC =
            Topic.create("LegadoReader.ReadingEvent", ReadingEventListener.class);

    /**
     * 处理阅读事件
     *
     * @param event 阅读事件
     */
    void onReadingEvent(ReadingEvent event);
}
