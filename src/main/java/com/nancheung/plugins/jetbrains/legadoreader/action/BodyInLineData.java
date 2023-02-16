package com.nancheung.plugins.jetbrains.legadoreader.action;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;
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
    
    /**
     * 是否启用在代码行中显示正文
     */
    @Getter
    @Setter
    private boolean enableShowBodyInLine = true;
    
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
    
    /**
     * 上一行
     */
    public void previousLine() {
        int currentLineIndex = currentLine.getLineIndex();
        
        if (currentLineIndex > 0) {
            currentLine = lineContentList.get(currentLineIndex - 1);
        }
    }
    
    /**
     * 下一行
     */
    public void nextLine() {
        int currentLineIndex = currentLine.getLineIndex();
        
        if (currentLineIndex < lineContentList.size() - 1) {
            currentLine = lineContentList.get(currentLineIndex + 1);
        }
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
