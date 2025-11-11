package com.nancheung.plugins.jetbrains.legadoreader.manager;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 行内阅读数据管理器
 * 线程安全的 Application Service，用于管理编辑器行内阅读的分页数据
 */
@Slf4j
@Service
public final class BodyInLineDataManager {
    private static final int DEFAULT_LINE_MAX_LENGTH = 30;

    /**
     * 当前显示的页
     */
    private final AtomicReference<LineData> currentLine = new AtomicReference<>();

    /**
     * 当前章节的所有分页列表
     */
    private final AtomicReference<List<LineData>> lineContentList = new AtomicReference<>(Collections.emptyList());

    /**
     * 当前章节的完整正文内容，可能为 null，用于内容变更检测
     *
     */
    @Getter
    private volatile String bodyContent;

    /**
     * 每页最大字符数
     *
     */
    @Getter
    private volatile int lineMaxLength = DEFAULT_LINE_MAX_LENGTH;

    public static BodyInLineDataManager getInstance() {
        return ApplicationManager.getApplication().getService(BodyInLineDataManager.class);
    }

    /**
     * 获取当前显示的页
     * 如果内容发生变化，会自动重新初始化
     *
     * @return 当前页数据，如果没有内容则返回 null
     */
    public LineData getCurrentLine() {
        try {
            String currentContent = ReadingSessionManager.getInstance().getCurrentContent();

            // 内容为空时返回 null
            if (StrUtil.isEmpty(currentContent)) {
                log.debug("当前章节内容为空");
                return null;
            }

            // 检测内容是否变化，如果变化则重新初始化
            if (currentLine.get() == null || !currentContent.equals(bodyContent)) {
                log.debug("检测到内容变化，重新初始化分页");
                initCurrent(currentContent);
            }

            return currentLine.get();
        } catch (Exception e) {
            log.error("获取当前页失败", e);
            return null;
        }
    }

    /**
     * 设置当前显示的页
     *
     * @param line 要设置的页数据
     */
    public void setCurrentLine(LineData line) {
        if (line != null) {
            currentLine.set(line);
            log.debug("切换到第 " + (line.getLineIndex() + 1) + " 页");
        }
    }

    /**
     * 获取当前章节的所有分页列表
     *
     * @return 分页列表，不会返回 null
     */
    public List<LineData> getLineContentList() {
        List<LineData> list = lineContentList.get();
        return list != null ? list : Collections.emptyList();
    }

    /**
     * 设置每页最大字符数
     *
     * @param length 字符数，必须大于 0
     */
    public void setLineMaxLength(int length) {
        if (length > 0) {
            this.lineMaxLength = length;
            log.debug("设置每页最大字符数为 " + length);
        }
    }

    /**
     * 初始化当前章节的分页数据
     * 该方法是线程安全的
     *
     * @param content 章节内容
     */
    public synchronized void initCurrent(String content) {
        try {
            bodyContent = content;

            if (StrUtil.isEmpty(content)) {
                log.warn("初始化失败：章节内容为空");
                lineContentList.set(Collections.emptyList());
                currentLine.set(null);
                return;
            }

            // 分页
            List<LineData> pages = splitLines(content, lineMaxLength);
            lineContentList.set(pages);

            // 设置第一页为当前页
            if (!pages.isEmpty()) {
                currentLine.set(pages.get(0));
                log.info("章节分页完成，共 " + pages.size() + " 页");
            } else {
                currentLine.set(null);
                log.warn("分页结果为空");
            }
        } catch (Exception e) {
            log.error("初始化分页数据失败", e);
            lineContentList.set(Collections.emptyList());
            currentLine.set(null);
        }
    }

    /**
     * 初始化当前章节的分页数据（使用当前会话的内容）
     */
    public void initCurrent() {
        String content = ReadingSessionManager.getInstance().getCurrentContent();
        initCurrent(content);
    }

    /**
     * 将完整章节内容按固定长度分页
     * 改进版：正确处理 Unicode 代理对（如 emoji）
     *
     * @param body      完整章节内容
     * @param maxLength 每页最大字符数
     * @return 分页列表
     */
    private List<LineData> splitLines(String body, int maxLength) {
        if (StrUtil.isEmpty(body)) {
            return Collections.emptyList();
        }

        List<LineData> lineDataList = new ArrayList<>();
        int start = 0;
        int pageIndex = 0;

        while (start < body.length()) {
            // 计算本页的结束位置
            int end = Math.min(start + maxLength, body.length());

            // 避免在 Unicode 代理对中间截断（如 emoji）
            // 高代理（High Surrogate）范围：U+D800 到 U+DBFF
            if (end < body.length() && Character.isHighSurrogate(body.charAt(end - 1))) {
                end--;
                log.debug("检测到 Unicode 代理对，调整分页位置");
            }

            // 提取本页内容
            String pageContent = body.substring(start, end);

            // 创建 LineData 对象
            LineData lineData = new LineData();
            lineData.setLineIndex(pageIndex);
            lineData.setLineStratIndex(start);
            lineData.setLineEndIndex(end);
            lineData.setLineContent(pageContent);

            lineDataList.add(lineData);

            // 移动到下一页
            start = end;
            pageIndex++;
        }

        return lineDataList;
    }

    /**
     * 清空所有数据
     */
    public synchronized void clear() {
        currentLine.set(null);
        lineContentList.set(Collections.emptyList());
        bodyContent = null;
        log.debug("清空行内阅读数据");
    }

    /**
     * 单页数据
     */
    @Data
    public static class LineData {
        /**
         * 页码索引（从 0 开始）
         */
        private int lineIndex;

        /**
         * 在完整正文中的字符起始位置
         */
        private int lineStratIndex;

        /**
         * 在完整正文中的字符结束位置
         */
        private int lineEndIndex;

        /**
         * 当前页的文本内容
         */
        private String lineContent;

        /**
         * 格式化显示（用于在编辑器行尾显示）
         *
         * @param totalPages 总页数
         * @return 格式化后的文本，例如 "   3/45  这是第三页的内容..."
         */
        public String formatForDisplay(int totalPages) {
            int currentPage = lineIndex + 1;
            return String.format("   %d/%d  %s", currentPage, totalPages, lineContent);
        }
    }
}
