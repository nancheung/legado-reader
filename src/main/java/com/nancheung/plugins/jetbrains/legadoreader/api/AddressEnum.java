package com.nancheung.plugins.jetbrains.legadoreader.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddressEnum {
    /**
     * 获取书架目录
     */
    GET_BOOKSHELF("/getBookshelf"),
    
    /**
     * 获取正文内容
     */
    GET_BOOK_CONTENT("/getBookContent"),
    ;
    
    private final String address;
}
