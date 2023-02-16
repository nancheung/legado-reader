package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.nancheung.plugins.jetbrains.legadoreader.action.BodyInLineData;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 在行内阅读
 *
 * @author erqian.zn
 */
public class ReaderEditorLinePainter extends EditorLinePainter {
    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {
        // 判断是否启用了行内阅读
        if (!BodyInLineData.isEnableShowBodyInLine()) {
            return null;
        }
        
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setForegroundColor(Data.textBodyFontColor);
        textAttributes.setFontType(Font.ITALIC);
        
        // 获取当前编辑器信息
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        ReaderInLineApplicationService.getInstance(project);
        
        // 只有当前光标所在之处才会显示
        if (editor != null && lineNumber == editor.getCaretModel().getLogicalPosition().line) {
            Optional<BodyInLineData.LineData> currentLineOpt = Optional.ofNullable(BodyInLineData.getCurrentLine());
            
            return currentLineOpt.map(lineData -> String.format("   %s/%s  %s",
                            lineData.getLineIndex() + 1, BodyInLineData.getLineContentList().size(), lineData.getLineContent())
                    ).map(text -> Collections.singleton(new LineExtensionInfo(text, textAttributes))).orElse(null);
        }
        
        return null;
    }
    
    public static ReaderEditorLinePainter getInstance() {
        return ApplicationManager.getApplication().getService(ReaderEditorLinePainter.class);
    }
}
