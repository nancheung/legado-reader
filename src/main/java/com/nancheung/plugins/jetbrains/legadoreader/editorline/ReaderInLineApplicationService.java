package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import lombok.Getter;

public class ReaderInLineApplicationService {
    
    @Getter
    private final Project project;
    
    public ReaderInLineApplicationService(Project project) {
        this.project = project;
        
        // 获取文件编辑器管理器实例
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        // 获取当前活动的编辑器
        Editor editor = editorManager.getSelectedTextEditor();
        // 如果当前编辑器不为空，则向其添加 MouseWheelListener
        if (editor != null) {
            editor.addEditorMouseListener(new SwitchLineMouseListener());
        }
    }
    
    public static ReaderInLineApplicationService getInstance(Project project) {
        return project.getService(ReaderInLineApplicationService.class);
    }
    
}
