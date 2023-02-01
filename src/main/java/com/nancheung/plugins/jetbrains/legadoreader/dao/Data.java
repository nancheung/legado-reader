package com.nancheung.plugins.jetbrains.legadoreader.dao;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;

import java.util.List;
import java.util.Map;

/**
 * 数据
 *
 * @author NanCheung
 */
public class Data {
    public static String IP;
    
    /**
     * 书架目录
     * key: author +"#"+ name
     * value: 书籍信息
     */
    public static Map<String, BookDTO> bookshelf;
    
    /**
     * 当前书籍目录
     */
    public static List<BookChapterDTO> currentBookChapterList;
    
    /**
     * 当前书籍
     */
    public static BookDTO currentBook;
    
    /**
     * 当前书籍章节索引
     */
    public static int currentBookIndex;
    
    public static BookDTO getBook(String author, String name) {
        return bookshelf.get(author + "#" + name);
    }
}
