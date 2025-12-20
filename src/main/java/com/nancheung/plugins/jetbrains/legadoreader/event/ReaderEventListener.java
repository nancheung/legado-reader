package com.nancheung.plugins.jetbrains.legadoreader.event;

import com.intellij.util.messages.Topic;

/**
 * 阅读器事件监听器
 * 使用 IntelliJ MessageBus 实现事件发布/订阅
 *
 * @author NanCheung
 */
public interface ReaderEventListener {

    /**
     * Application 级别的事件主题
     * 事件在所有 IDE 窗口间共享
     */
    @Topic.AppLevel
    Topic<ReaderEventListener> TOPIC =
            Topic.create("LegadoReader.Event", ReaderEventListener.class);

    /**
     * 处理事件
     *
     * @param event 事件对象
     */
    void onEvent(ReaderEvent event);
}
