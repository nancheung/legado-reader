package com.nancheung.plugins.jetbrains.legadoreader.command;

/**
 * 用户指令类型枚举
 * 定义所有用户可发起的指令
 *
 * @author NanCheung
 */
public enum CommandType {
    // ========== 书架指令 ==========
    /**
     * 获取书架列表
     */
    FETCH_BOOKSHELF("获取书架列表"),

    /**
     * 刷新书架
     */
    REFRESH_BOOKSHELF("刷新书架"),

    // ========== 阅读指令 ==========
    /**
     * 选择书籍进入阅读（从书架点击）
     */
    SELECT_BOOK("选择书籍"),

    /**
     * 跳转到指定章节
     */
    JUMP_TO_CHAPTER("跳转章节"),

    /**
     * 下一章
     */
    NEXT_CHAPTER("下一章"),

    /**
     * 上一章
     */
    PREVIOUS_CHAPTER("上一章"),

    /**
     * 下一页
     */
    NEXT_PAGE("下一页"),

    /**
     * 上一页
     */
    PREVIOUS_PAGE("上一页"),

    // ========== 会话指令 ==========
    /**
     * 返回书架（结束阅读会话）
     */
    BACK_TO_BOOKSHELF("返回书架"),

    /**
     * 获取当前阅读信息
     */
    GET_READING_INFO("获取阅读信息"),

    /**
     * 切换阅读模式显示/隐藏
     */
    TOGGLE_READING_MODE("切换阅读模式");

    private final String description;

    CommandType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
