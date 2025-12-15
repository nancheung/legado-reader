package com.nancheung.plugins.jetbrains.legadoreader.common;

/**
 * 阅读器动作接口
 *
 * @author NanCheung
 */
public interface IReader {
    /**
     * 预载章节的进度因子
     */
    float DEFAULT_LOAD_FACTOR = 0.5f;
    
    /**
     * 上一页
     */
    void previousPage();

    /**
     * 下一页
     */
    void nextPage();

    /**
     * 将整章内容分页
     *
     * @param chapterContent 章节内容
     * @param pageSize       每页大小
     */
    void splitChapter(String chapterContent, int pageSize);
}
