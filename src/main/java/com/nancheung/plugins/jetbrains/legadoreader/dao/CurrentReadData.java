package com.nancheung.plugins.jetbrains.legadoreader.dao;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 当前阅读数据
 *
 * @author NanCheung
 */
@UtilityClass
public class CurrentReadData {
    /**
     * 当前书籍
     */
    @Getter
    @Setter
    private BookDTO book;
    
    /**
     * 当前书籍目录
     */
    @Getter
    @Setter
    private List<BookChapterDTO> bookChapterList;
    
    /**
     * 当前书籍章节索引
     */
    @Getter
    @Setter
    private int bookIndex;
    
    /**
     * 当前书籍章节正文内容
     */
    @Getter
    @Setter
    private String bodyContent;
    
    /**
     * 当前书籍章节内容
     */
    public BookChapterDTO getBookChapter() {
        return CurrentReadData.bookChapterList.get(bookIndex);
    }
    
    public void indexAtomicIncrement() {
        bookIndex++;
    }
    
    public void indexAtomicDecrement() {
        bookIndex--;
    }
}
