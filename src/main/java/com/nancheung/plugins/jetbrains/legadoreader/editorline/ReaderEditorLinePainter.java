package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.nancheung.plugins.jetbrains.legadoreader.dao.BodyInLineData;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;

/**
 * 在行内阅读
 *
 * @author erqian.zn
 */
public class ReaderEditorLinePainter extends EditorLinePainter {
    
    private static final Set<Editor> EDITORS = new HashSet<>();
    
    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {
        // 判断是否启用了行内阅读
        if (!Data.enableShowBodyInLine) {
            return null;
        }
        
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setForegroundColor(Data.textBodyFontColor);
        textAttributes.setFontType(Font.ITALIC);
        
        // 获取当前编辑器信息
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor==null) {
            return null;
        }
        
        // 如果当前编辑器不为空，则向其添加 MouseWheelListener
        if (!EDITORS.contains(editor)) {
            editor.addEditorMouseListener(new SwitchLineMouseListener());
            EDITORS.add(editor);
        }
        
        // 只在当前光标所在行显示
        if (lineNumber == editor.getCaretModel().getLogicalPosition().line ) {
            Optional<BodyInLineData.LineData> currentLineOpt = Optional.ofNullable(BodyInLineData.getCurrentLine());
        
            return currentLineOpt.map(lineData -> String.format("   %s/%s  %s",
                    lineData.getLineIndex() + 1, BodyInLineData.getLineContentList().size(), lineData.getLineContent())
            ).map(text -> Collections.singleton(new LineExtensionInfo(text, textAttributes))).orElse(null);
        }
        
        return null;
    }
}
