package com.nancheung.plugins.jetbrains.legadoreader.dao;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据
 *
 * @author NanCheung
 */
@UtilityClass
public class Data {
    public String address;
    
    public Color textBodyFontColor;
    
    public Font textBodyFont;

    /**
     * 书架目录
     * key: author +"#"+ name
     * value: 书籍信息
     */
    public Map<String, BookDTO> bookshelf;
    
    private final BiFunction<String, String, String> BOOK_MAP_KEY_FUNC = (author, name) -> author + "#" + name;
    
    
    public BookDTO getBook(String author, String name) {
        return bookshelf.get(BOOK_MAP_KEY_FUNC.apply(author, name));
    }
    
    public void setBookshelf(List<BookDTO> books) {
        Data.bookshelf = books.stream().collect(Collectors.toMap(book -> BOOK_MAP_KEY_FUNC.apply(book.getAuthor(), book.getName()), Function.identity()));
    }
}
