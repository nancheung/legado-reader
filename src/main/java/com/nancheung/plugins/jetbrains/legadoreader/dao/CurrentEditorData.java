package com.nancheung.plugins.jetbrains.legadoreader.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CurrentEditorData {
    
    @Getter
    @Setter
    private int currentLine;
}
