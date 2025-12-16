package com.nancheung.plugins.jetbrains.legadoreader.model;

/**
 * 阅读会话状态枚举
 * 定义阅读会话的所有可能状态
 *
 * @author NanCheung
 */
public enum ReadingSessionState {
    /**
     * 空闲状态（没有阅读会话）
     */
    IDLE("空闲"),

    /**
     * 加载中（正在获取书籍/章节数据）
     */
    LOADING("加载中"),

    /**
     * 阅读中（章节内容已加载，用户正在阅读）
     */
    READING("阅读中"),

    /**
     * 翻页中（页内翻页）
     */
    PAGING("翻页中"),

    /**
     * 错误状态（加载失败或其他错误）
     */
    ERROR("错误");

    private final String description;

    ReadingSessionState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
