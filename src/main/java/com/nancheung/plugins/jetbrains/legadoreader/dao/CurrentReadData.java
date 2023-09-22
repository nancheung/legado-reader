package com.nancheung.plugins.jetbrains.legadoreader.dao;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * 书架目录
     * key: author +"#"+ name
     * value: 书籍信息
     */
    private Map<String, BookDTO> bookshelf;

    private final BiFunction<String, String, String> BOOK_MAP_KEY_FUNC = (author, name) -> author + "#" + name;
    
    /**
     * 当前书籍章节内容
     */
    public BookChapterDTO getBookChapter() {
        return CurrentReadData.bookChapterList.get(bookIndex);
    }

    public BookDTO getBook(String author, String name) {
        return bookshelf.get(BOOK_MAP_KEY_FUNC.apply(author, name));
    }

    public void setBookshelf(List<BookDTO> books) {
        CurrentReadData.bookshelf = books.stream().collect(Collectors.toMap(book -> BOOK_MAP_KEY_FUNC.apply(book.getAuthor(), book.getName()), Function.identity()));
    }
    
    public void indexAtomicIncrement() {
        bookIndex++;
    }
    
    public void indexAtomicDecrement() {
        bookIndex--;
    }
}
