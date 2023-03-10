package com.nancheung.plugins.jetbrains.legadoreader.dao;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BodyInLineData {
    /**
     * 当前行
     */
    @Setter
    private LineData currentLine;
    
    /**
     * 当前章的所有行内容
     */
    @Getter
    @Setter
    private List<LineData> lineContentList;
    
    /**
     * 当前章节完整正文内容，用于分行与校验数据
     */
    @Getter
    @Setter
    private String bodyContent;
    
    /**
     * 行最大长度
     */
    @Getter
    @Setter
    private int lineMaxLength = 30;
    
    @Data
    public class LineData {
        /**
         * 当前行号索引
         */
        private int lineIndex;
        
        /**
         * 当前行的字符开始索引
         */
        private int lineStratIndex;
        
        /**
         * 当前行的字符结束索引
         */
        private int lineEndIndex;
        
        /**
         * 当前行的内容
         */
        private String lineContent;
    }
    
    static {
        initCurrent();
    }
    
    /**
     * 获取当前行
     */
    public LineData getCurrentLine() {
        if (currentLine == null || !bodyContent.equals(CurrentReadData.getBodyContent())) {
            initCurrent();
        }
        
        return currentLine;
    }
    
    
    public void initCurrent() {
        bodyContent = CurrentReadData.getBodyContent();
        if (StrUtil.isEmpty(bodyContent)) {
            return;
        }
        
        lineContentList = splitLines(bodyContent, lineMaxLength);
        currentLine = lineContentList.get(0);
    }
    
    private List<LineData> splitLines(String body, int lineMaxLength) {
        List<String> lineStrList = CollectionUtil.toList(StrUtil.split(body, lineMaxLength));
        
        List<LineData> lineDataList = new ArrayList<>();
        
        for (int i = 0; i < lineStrList.size(); i++) {
            LineData lineData = new LineData();
            
            lineData.setLineIndex(i);
            lineData.setLineStratIndex(i * lineMaxLength);
            lineData.setLineEndIndex((i + 1) * lineMaxLength);
            lineData.setLineContent(lineStrList.get(i));
            
            lineDataList.add(lineData);
        }
        
        return lineDataList;
    }
}
