package com.nancheung.plugins.jetbrains.legadoreader.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一分页管理器（Application Service）
 * 供所有阅读模式共享使用
 * 线程安全，使用 AtomicReference 管理状态
 *
 * @author NanCheung
 */
@Slf4j
@Service
public final class PaginationManager implements IPaginationManager {

    /**
     * 默认每页字符数
     */
    private static final int DEFAULT_PAGE_SIZE = 30;

    /**
     * 当前页
     */
    private final AtomicReference<PageData> currentPage = new AtomicReference<>();

    /**
     * 所有页数据（不可变列表）
     */
    private final AtomicReference<List<PageData>> pages = new AtomicReference<>(Collections.emptyList());

    /**
     * 源内容
     */
    private volatile String sourceContent;

    /**
     * 每页大小
     */
    private volatile int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 获取单例实例
     */
    public static PaginationManager getInstance() {
        return ApplicationManager.getApplication().getService(PaginationManager.class);
    }

    @Override
    public void paginate(String content, int pageSize) {
        this.sourceContent = content;
        this.pageSize = pageSize;

        if (content == null || content.isEmpty()) {
            pages.set(Collections.emptyList());
            currentPage.set(null);
            log.debug("分页完成：内容为空");
            return;
        }

        List<PageData> newPages = splitIntoPages(content, pageSize);
        pages.set(Collections.unmodifiableList(newPages));

        // 默认定位到第一页
        if (!newPages.isEmpty()) {
            currentPage.set(newPages.get(0));
        }

        log.info("分页完成，共 {} 页", newPages.size());
    }

    @Override
    @Nullable
    public PageData getCurrentPage() {
        return currentPage.get();
    }

    @Override
    @Nullable
    public PageData nextPage() {
        PageData current = currentPage.get();
        List<PageData> pageList = pages.get();

        if (current == null || pageList.isEmpty()) {
            return null;
        }

        int nextIndex = current.pageIndex() + 1;
        if (nextIndex >= pageList.size()) {
            log.debug("已经是最后一页");
            return null; // 已是最后一页
        }

        PageData next = pageList.get(nextIndex);
        currentPage.set(next);
        log.debug("翻到下一页: {}/{}", nextIndex + 1, pageList.size());
        return next;
    }

    @Override
    @Nullable
    public PageData previousPage() {
        PageData current = currentPage.get();
        List<PageData> pageList = pages.get();

        if (current == null || pageList.isEmpty()) {
            return null;
        }

        int prevIndex = current.pageIndex() - 1;
        if (prevIndex < 0) {
            log.debug("已经是第一页");
            return null; // 已是第一页
        }

        PageData prev = pageList.get(prevIndex);
        currentPage.set(prev);
        log.debug("翻到上一页: {}/{}", prevIndex + 1, pageList.size());
        return prev;
    }

    @Override
    @Nullable
    public PageData goToPage(int pageIndex) {
        List<PageData> pageList = pages.get();

        if (pageList.isEmpty() || pageIndex < 0 || pageIndex >= pageList.size()) {
            log.warn("无效的页码索引: {}", pageIndex);
            return null;
        }

        PageData target = pageList.get(pageIndex);
        currentPage.set(target);
        log.debug("跳转到第 {} 页/{}", pageIndex + 1, pageList.size());
        return target;
    }

    @Override
    public int getTotalPages() {
        return pages.get().size();
    }

    /**
     * 定位到第一页
     *
     * @return 第一页数据，如果没有页则返回 null
     */
    @Nullable
    public PageData goToFirstPage() {
        return goToPage(0);
    }

    /**
     * 定位到最后一页
     *
     * @return 最后一页数据，如果没有页则返回 null
     */
    @Nullable
    public PageData goToLastPage() {
        int totalPages = getTotalPages();
        return totalPages > 0 ? goToPage(totalPages - 1) : null;
    }

    /**
     * 获取当前每页大小
     *
     * @return 每页字符数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页大小并重新分页
     *
     * @param pageSize 每页字符数
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        if (sourceContent != null && !sourceContent.isEmpty()) {
            paginate(sourceContent, pageSize);
        }
    }

    /**
     * 清空分页数据
     */
    public void clear() {
        pages.set(Collections.emptyList());
        currentPage.set(null);
        sourceContent = null;
        log.debug("清空分页数据");
    }

    /**
     * 将内容分割为页
     * 智能处理 Unicode 代理对（如 emoji），避免在字符中间截断
     *
     * @param content    完整内容
     * @param maxLength  每页最大字符数
     * @return 页数据列表
     */
    private List<PageData> splitIntoPages(String content, int maxLength) {
        List<PageData> result = new ArrayList<>();
        int start = 0;
        int pageIndex = 0;

        while (start < content.length()) {
            int end = Math.min(start + maxLength, content.length());

            // 避免在 Unicode 代理对中间截断
            // 代理对：高代理（U+D800 到 U+DBFF）+ 低代理（U+DC00 到 U+DFFF）
            // 例如 emoji "😀" 由两个 char 组成
            if (end < content.length() && Character.isHighSurrogate(content.charAt(end - 1))) {
                end--; // 回退一个字符，避免截断 emoji
            }

            String pageContent = content.substring(start, end);
            result.add(new PageData(pageIndex, start, end, pageContent));

            start = end;
            pageIndex++;
        }

        return result;
    }
}
