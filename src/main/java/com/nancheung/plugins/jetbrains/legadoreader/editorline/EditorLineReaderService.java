package com.nancheung.plugins.jetbrains.legadoreader.editorline;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.nancheung.plugins.jetbrains.legadoreader.dao.BodyInLineData;
import com.nancheung.plugins.jetbrains.legadoreader.common.IReader;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;

import java.util.ArrayList;
import java.util.List;

public class EditorLineReaderService implements IReader {
    /**
     * 上一页
     */
    @Override
    public void previousPage() {
        BodyInLineData.LineData currentLine = BodyInLineData.getCurrentLine();
        if (currentLine == null) {
            return;
        }
        
        int currentLineIndex = currentLine.getLineIndex();
        
        List<BodyInLineData.LineData> lineContentList = BodyInLineData.getLineContentList();
        if (currentLineIndex > 0) {
            BodyInLineData.setCurrentLine(lineContentList.get(currentLineIndex - 1));
        }
        
        // 判断当前行小于总行数的50%，则预载上一章
        if (currentLineIndex < lineContentList.size() * DEFAULT_LOAD_FACTOR) {
            // TODO: 2023/3/2 预载上一章
        }
    }
    
    /**
     * 下一页
     */
    @Override
    public void nextPage() {
        BodyInLineData.LineData currentLine = BodyInLineData.getCurrentLine();
        if (currentLine == null) {
            return;
        }
        
        int currentLineIndex = currentLine.getLineIndex();
        
        List<BodyInLineData.LineData> lineContentList = BodyInLineData.getLineContentList();
        
        // 如果当前行不是最后一行，则设置下一行为当前行
        if (currentLineIndex < lineContentList.size() - 1) {
            BodyInLineData.setCurrentLine(lineContentList.get(currentLineIndex + 1));
        }
        
        // 判断当前行大于总行数的50%，则预载下一章
        if (currentLineIndex > lineContentList.size() * DEFAULT_LOAD_FACTOR) {
            // TODO: 2023/3/2 预载下一章
        }
    }
    
    /**
     * 上一章
     */
    @Override
    public void previousChapter() {
        // 休眠3秒
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 下一章
     */
    @Override
    public void nextChapter() {
    
    }
    
    /**
     * 将整章内容分页
     *
     * @param chapterContent 章节内容
     * @param pageSize       每页大小
     */
    @Override
    public void splitChapter(String chapterContent, int pageSize) {
        String bodyContent = CurrentReadData.getBodyContent();
        BodyInLineData.setBodyContent(bodyContent);
        
        if (StrUtil.isEmpty(bodyContent)) {
            return;
        }
        
        List<String> lineStrList = CollectionUtil.toList(StrUtil.split(chapterContent, pageSize));
        
        List<BodyInLineData.LineData> lineDataList = new ArrayList<>();
        
        for (int i = 0; i < lineStrList.size(); i++) {
            BodyInLineData.LineData lineData = new BodyInLineData.LineData();
            
            lineData.setLineIndex(i);
            lineData.setLineStratIndex(i * pageSize);
            lineData.setLineEndIndex((i + 1) * pageSize);
            lineData.setLineContent(lineStrList.get(i));
            
            lineDataList.add(lineData);
        }
        BodyInLineData.setLineContentList(lineDataList);
        BodyInLineData.setCurrentLine(lineDataList.get(0));
    }
    
    
}
