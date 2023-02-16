package com.nancheung.plugins.jetbrains.legadoreader.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.EditorEventListener;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentEditorData;
import org.jetbrains.annotations.NotNull;

public class EditorAction extends AnAction {
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        
        // 获取当前行
        SelectionModel selectionModel = editor.getSelectionModel();
    
        selectionModel.addSelectionListener(new EditorEventListener() {
            @Override
            public void selectionChanged(@NotNull SelectionEvent e) {
                EditorEventListener.super.selectionChanged(e);
                int leadSelectionOffset = e.getEditor().getSelectionModel().getLeadSelectionOffset();
                CurrentEditorData.setCurrentLine(leadSelectionOffset);
            }
        });
        
//        int currentLine = editor.getCaretModel().getLogicalPosition().line;
//        CurrentEditorData.setCurrentLine(currentLine);
    }
}
