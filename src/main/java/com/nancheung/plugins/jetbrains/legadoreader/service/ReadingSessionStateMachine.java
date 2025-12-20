package com.nancheung.plugins.jetbrains.legadoreader.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.nancheung.plugins.jetbrains.legadoreader.model.ReadingSessionState;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 阅读会话状态机（Application Service）
 * 管理阅读会话的生命周期状态转换
 * 确保状态转换的合法性
 *
 * @author NanCheung
 */
@Slf4j
@Service
public final class ReadingSessionStateMachine {

    /**
     * 当前状态
     */
    private final AtomicReference<ReadingSessionState> currentState =
            new AtomicReference<>(ReadingSessionState.IDLE);

    /**
     * 状态转换规则表
     * key: 当前状态
     * value: 允许转换到的目标状态集合
     */
    private static final Map<ReadingSessionState, Set<ReadingSessionState>> VALID_TRANSITIONS = Map.of(
            // 空闲状态 → 可以开始加载
            ReadingSessionState.IDLE,
            Set.of(ReadingSessionState.LOADING),

            // 加载中 → 可以成功进入阅读、失败进入错误、或取消回到空闲
            ReadingSessionState.LOADING,
            Set.of(ReadingSessionState.READING, ReadingSessionState.ERROR, ReadingSessionState.IDLE),

            // 阅读中 → 可以切换章节（加载）、翻页、或返回书架（空闲）
            ReadingSessionState.READING,
            Set.of(ReadingSessionState.LOADING, ReadingSessionState.PAGING, ReadingSessionState.IDLE),

            // 翻页中 → 可以完成翻页回到阅读、或切换章节（加载）
            ReadingSessionState.PAGING,
            Set.of(ReadingSessionState.READING, ReadingSessionState.LOADING),

            // 错误状态 → 可以重试（加载）或返回书架（空闲）
            ReadingSessionState.ERROR,
            Set.of(ReadingSessionState.IDLE, ReadingSessionState.LOADING)
    );

    /**
     * 获取单例实例
     */
    public static ReadingSessionStateMachine getInstance() {
        return ApplicationManager.getApplication().getService(ReadingSessionStateMachine.class);
    }

    /**
     * 尝试状态转换
     * 如果转换不合法，保持当前状态不变
     *
     * @param newState 目标状态
     * @return true 如果转换成功，false 如果转换不合法
     */
    public boolean transition(ReadingSessionState newState) {
        ReadingSessionState oldState = currentState.updateAndGet(current -> {
            Set<ReadingSessionState> allowedStates = VALID_TRANSITIONS.getOrDefault(current, Set.of());
            if (allowedStates.contains(newState)) {
                log.debug("状态转换: {} → {}", current, newState);
                return newState;
            } else {
                log.warn("非法状态转换: {} → {}，保持当前状态", current, newState);
                return current;
            }
        });

        return oldState != newState || oldState == currentState.get();
    }

    /**
     * 强制设置状态（慎用）
     * 跳过状态转换规则检查
     *
     * @param state 目标状态
     */
    public void forceSet(ReadingSessionState state) {
        ReadingSessionState oldState = currentState.getAndSet(state);
        log.warn("强制状态转换: {} → {}", oldState, state);
    }

    /**
     * 获取当前状态
     *
     * @return 当前状态
     */
    public ReadingSessionState getState() {
        return currentState.get();
    }

    /**
     * 是否正在阅读
     *
     * @return true 如果当前处于阅读状态
     */
    public boolean isReading() {
        return currentState.get() == ReadingSessionState.READING;
    }

    /**
     * 是否空闲（无阅读会话）
     *
     * @return true 如果当前处于空闲状态
     */
    public boolean isIdle() {
        return currentState.get() == ReadingSessionState.IDLE;
    }

    /**
     * 是否正在加载
     *
     * @return true 如果当前处于加载状态
     */
    public boolean isLoading() {
        return currentState.get() == ReadingSessionState.LOADING;
    }

    /**
     * 是否处于错误状态
     *
     * @return true 如果当前处于错误状态
     */
    public boolean isError() {
        return currentState.get() == ReadingSessionState.ERROR;
    }

    /**
     * 重置到空闲状态
     */
    public void reset() {
        ReadingSessionState oldState = currentState.getAndSet(ReadingSessionState.IDLE);
        log.debug("重置状态: {} → IDLE", oldState);
    }
}
