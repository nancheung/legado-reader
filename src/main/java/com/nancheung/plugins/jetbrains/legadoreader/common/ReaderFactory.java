package com.nancheung.plugins.jetbrains.legadoreader.common;

import com.nancheung.plugins.jetbrains.legadoreader.editorline.EditorLineReaderService;
import com.nancheung.plugins.jetbrains.legadoreader.toolwindow.ToolWindowReaderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum ReaderFactory {
    /**
     * 编辑器行内阅读
     */
    EDITOR_LINE(new EditorLineReaderService()),
    /**
     * 工具窗口阅读
     */
    TOOL_WINDOW(new ToolWindowReaderService()),
    ;
    
    @Getter
    private final IReader reader;
    
    public static final ReaderFactory[] READER_FACTORYS = values();
}
