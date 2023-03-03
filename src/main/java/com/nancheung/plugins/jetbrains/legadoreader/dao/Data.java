package com.nancheung.plugins.jetbrains.legadoreader.dao;

import com.intellij.ide.util.PropertiesComponent;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private String address;
    
    /**
     * 历史address
     */
    private Map<String, LocalDateTime> addressHistory = new LinkedHashMap<>(4);
    
    public Color textBodyFontColor;
    
    public Font textBodyFont;

    /**
     * 书架目录
     * key: author +"#"+ name
     * value: 书籍信息
     */
    public Map<String, BookDTO> bookshelf;
    
    private final BiFunction<String, String, String> BOOK_MAP_KEY_FUNC = (author, name) -> author + "#" + name;
    
    static {
        // 从本地读取历史address
        String addressHistoryStr = PropertiesComponent.getInstance().getValue(Constant.PLUGIN__PERSISTENCE_DATA + ".addressHistory");
        
        if (addressHistoryStr != null) {
            String[] addressHistoryArr = addressHistoryStr.split("\\{nc}");
            for (String address : addressHistoryArr) {
                Data.addressHistory.put(address, LocalDateTime.now());
            }
        }
    }
    
    public void addAddress(String address) {
        Data.addressHistory.put(address, LocalDateTime.now());
        Data.address = address;
        
        // 只保留最近的4条记录
        if (Data.addressHistory.size() > 4) {
            Data.addressHistory = Data.addressHistory.entrySet().stream()
                    .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                    .limit(4)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        
        // 持久化到本地
        PropertiesComponent.getInstance().setValue(Constant.PLUGIN__PERSISTENCE_DATA + ".addressHistory", String.join("{nc}", getAddressHistory()));
    }
    
    public String getAddress() {
        return address;
    }
    
    public List<String> getAddressHistory() {
        // addressHistory的key转为list，按照value排序
        return addressHistory.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
    }
    
    
    public BookDTO getBook(String author, String name) {
        return bookshelf.get(BOOK_MAP_KEY_FUNC.apply(author, name));
    }
    
    public void setBookshelf(List<BookDTO> books) {
        Data.bookshelf = books.stream().collect(Collectors.toMap(book -> BOOK_MAP_KEY_FUNC.apply(book.getAuthor(), book.getName()), Function.identity()));
    }
}
