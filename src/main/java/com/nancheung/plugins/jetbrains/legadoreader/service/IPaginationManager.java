package com.nancheung.plugins.jetbrains.legadoreader.service;

import org.jetbrains.annotations.Nullable;

/**
 * 分页管理器接口
 * 抽象分页行为，支持不同阅读模式
 *
 * @author NanCheung
 */
public interface IPaginationManager {

    /**
     * 初始化分页（重新分页）
     *
     * @param content  章节内容
     * @param pageSize 每页大小（字符数）
     */
    void paginate(String content, int pageSize);

    /**
     * 获取当前页
     *
     * @return 当前页数据，如果没有则返回 null
     */
    @Nullable
    PageData getCurrentPage();

    /**
     * 下一页
     *
     * @return 新的当前页，如果已是最后一页返回 null
     */
    @Nullable
    PageData nextPage();

    /**
     * 上一页
     *
     * @return 新的当前页，如果已是第一页返回 null
     */
    @Nullable
    PageData previousPage();

    /**
     * 跳转到指定页
     *
     * @param pageIndex 页码索引（从 0 开始）
     * @return 目标页数据，如果索引无效返回 null
     */
    @Nullable
    PageData goToPage(int pageIndex);

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    int getTotalPages();

    /**
     * 是否是第一页
     *
     * @return true 如果当前是第一页
     */
    default boolean isFirstPage() {
        PageData current = getCurrentPage();
        return current != null && current.pageIndex() == 0;
    }

    /**
     * 是否是最后一页
     *
     * @return true 如果当前是最后一页
     */
    default boolean isLastPage() {
        PageData current = getCurrentPage();
        return current != null && current.pageIndex() >= getTotalPages() - 1;
    }

    /**
     * 页数据（不可变）
     *
     * @param pageIndex 页码索引（从 0 开始）
     * @param startPos  在完整内容中的起始位置
     * @param endPos    在完整内容中的结束位置
     * @param content   当前页的文本内容
     */
    record PageData(
            int pageIndex,
            int startPos,
            int endPos,
            String content
    ) {
        /**
         * 格式化为显示文本
         * 格式："   页码/总页数  内容..."
         *
         * @param totalPages 总页数
         * @return 格式化后的文本
         */
        public String formatForDisplay(int totalPages) {
            return String.format("   %d/%d  %s", pageIndex + 1, totalPages, content);
        }
    }
}
