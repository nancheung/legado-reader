package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.nancheung.plugins.jetbrains.legadoreader.manager.BodyInLineDataManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 编辑器行内阅读渲染器
 * 在编辑器代码行的末尾显示书籍内容
 *
 * @author erqian.zn
 */
@Slf4j
public class ReaderEditorLinePainter extends EditorLinePainter {

    /**
     * 使用 WeakHashMap 避免内存泄漏
     * 当 Editor 被关闭后，会自动从 Map 中移除
     */
    private static final Set<Editor> EDITORS = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 缓存 Editor 实例，避免高频调用 getSelectedTextEditor()
     */
    private Editor cachedEditor;
    private long lastEditorCheckTime;
    private static final long EDITOR_CACHE_DURATION_MS = 1000; // 缓存 1 秒

    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(
            @NotNull Project project,
            @NotNull VirtualFile file,
            int lineNumber) {

        // 判断是否启用了行内阅读
        if (!PluginSettingsManager.getInstance().isEnableShowBodyInLine()) {
            return null;
        }

        // 获取当前编辑器（使用缓存优化性能）
        Editor editor = getCachedEditor(project);
        if (editor == null) {
            return null;
        }

        // 如果当前编辑器未注册鼠标监听器，则添加
        // WeakHashMap 会自动清理已关闭的编辑器
        if (!EDITORS.contains(editor)) {
            editor.addEditorMouseListener(new SwitchLineMouseListener());
            EDITORS.add(editor);
            log.debug("为编辑器添加鼠标监听器");
        }

        // 只在当前光标所在行显示
        int currentLine = editor.getCaretModel().getLogicalPosition().line;
        if (lineNumber != currentLine) {
            return null;
        }

        // 获取当前页数据
        BodyInLineDataManager dataManager = BodyInLineDataManager.getInstance();
        BodyInLineDataManager.LineData currentPageData = dataManager.getCurrentLine();

        if (currentPageData == null) {
            // 没有阅读内容时不显示
            return null;
        }

        // 格式化显示文本
        int totalPages = dataManager.getLineContentList().size();
        String displayText = currentPageData.formatForDisplay(totalPages);

        // 设置文本样式
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setForegroundColor(PluginSettingsManager.getInstance().getTextBodyFontColor());
        textAttributes.setFontType(Font.ITALIC);

        return Collections.singleton(new LineExtensionInfo(displayText, textAttributes));


    }

    /**
     * 获取缓存的编辑器实例
     * 避免每次渲染都调用 getSelectedTextEditor()，提升性能
     *
     * @param project 当前项目
     * @return 编辑器实例，如果没有则返回 null
     */
    private Editor getCachedEditor(Project project) {
        long now = System.currentTimeMillis();

        // 如果缓存过期或未初始化，重新获取
        if (cachedEditor == null || now - lastEditorCheckTime > EDITOR_CACHE_DURATION_MS) {
            cachedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            lastEditorCheckTime = now;
        }

        return cachedEditor;
    }
}
