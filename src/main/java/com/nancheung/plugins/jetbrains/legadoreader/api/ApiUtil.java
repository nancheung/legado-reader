package com.nancheung.plugins.jetbrains.legadoreader.api;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookProgressDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.R;
import com.nancheung.plugins.jetbrains.legadoreader.dao.Data;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * API工具
 *
 * @author NanCheung
 */
@UtilityClass
public class ApiUtil {
    
    /**
     * 获取书架目录列表
     *
     * @return 书架目录列表
     */
    public List<BookDTO> getBookshelf() {
        String url = Data.getAddress() + AddressEnum.GET_BOOKSHELF.getAddress();
        String textBody = HttpUtil.get(url);
        R<List<BookDTO>> r = JSONUtil.toBean(textBody, new TypeReference<>() {
        }, true);
        
        System.out.println(AddressEnum.GET_BOOKSHELF.getAddress() + "：" + r.getIsSuccess() + "：" + r.getErrorMsg());
        
        return r.getData();
    }
    
    /**
     * 获取正文内容
     *
     * @return 正文内容
     */
    public String getBookContent(String bookUrl, int bookIndex) {
        // 调用API获取正文内容
        String url = Data.getAddress() + AddressEnum.GET_BOOK_CONTENT.getAddress() + "?url=" + URLUtil.encodeAll(bookUrl) + "&index=" + bookIndex;
        
        String textBody = HttpUtil.get(url);
        R<String> r = JSONUtil.toBean(textBody, new TypeReference<>() {
        }, true);
        
        System.out.println(AddressEnum.GET_BOOK_CONTENT.getAddress() + "：" + r.getIsSuccess() + "：" + r.getErrorMsg());
        
        return r.getData();
    }
    
    /**
     * 获取章节目录列表
     *
     * @return 章节目录列表
     */
    public List<BookChapterDTO> getChapterList(String bookUrl) {
        // 调用API获取书架目录
        String url = Data.getAddress() + AddressEnum.GET_CHAPTER_LIST.getAddress() + "?url=" + URLUtil.encodeAll(bookUrl);
        
        String textBody = HttpUtil.get(url);
        R<List<BookChapterDTO>> r = JSONUtil.toBean(textBody, new TypeReference<>() {
        }, true);
        
        System.out.println(AddressEnum.GET_CHAPTER_LIST.getAddress() + "：" + r.getIsSuccess() + "：" + r.getErrorMsg());
        
        return r.getData();
    }
    
    /**
     * 保存阅读进度
     */
    public void saveBookProgress(String author, String name, int index, String title,int durChapterPos) {
        // 调用API获取书架目录
        String url = Data.getAddress() + AddressEnum.SAVE_BOOK_PROGRESS.getAddress();
        
        BookProgressDTO bookProgressDTO = BookProgressDTO.builder()
                .author(author)
                .name(name)
                .durChapterIndex(index)
                .durChapterTitle(title)
                .durChapterTime(System.currentTimeMillis())
                .durChapterPos(durChapterPos)
                .build();
        
        String textBody = HttpUtil.post(url, JSONUtil.toJsonStr(bookProgressDTO));
        R<String> r = JSONUtil.toBean(textBody, new TypeReference<>() {
        }, true);
        
        System.out.println(AddressEnum.SAVE_BOOK_PROGRESS.getAddress() + "：" + r.getIsSuccess() + "：" + r.getErrorMsg());
    }
}
