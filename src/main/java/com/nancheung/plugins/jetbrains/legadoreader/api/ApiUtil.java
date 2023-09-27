package com.nancheung.plugins.jetbrains.legadoreader.api;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookChapterDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookProgressDTO;
import com.nancheung.plugins.jetbrains.legadoreader.api.dto.R;
import com.nancheung.plugins.jetbrains.legadoreader.dao.CurrentReadData;
import com.nancheung.plugins.jetbrains.legadoreader.properties.GlobalSettingProperties;
import com.nancheung.plugins.jetbrains.legadoreader.properties.PropertiesFactory;
import com.nancheung.plugins.jetbrains.legadoreader.properties.UserBehaviorProperties;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * API工具
 *
 * @author NanCheung
 */
@UtilityClass
public class ApiUtil {

    private static final GlobalSettingProperties GLOBAL_SETTING_PROPERTIES = PropertiesFactory.getGlobalSetting();
    private static final UserBehaviorProperties USER_BEHAVIOR_PROPERTIES = PropertiesFactory.getUserBehavior();

    /**
     * 获取书架目录列表
     *
     * @return 书架目录列表
     */
    public List<BookDTO> getBookshelf() {
        String url = USER_BEHAVIOR_PROPERTIES.getAddress() + AddressEnum.GET_BOOKSHELF.getAddress();

        R<List<BookDTO>> r = get(url, new TypeReference<>() {
        });

        return r.getData();
    }

    /**
     * 获取正文内容
     *
     * @return 正文内容
     */
    public String getBookContent(String bookUrl, int bookIndex) {
        // 调用API获取正文内容
        String url = USER_BEHAVIOR_PROPERTIES.getAddress() + AddressEnum.GET_BOOK_CONTENT.getAddress() + "?url=" + URLUtil.encodeAll(bookUrl) + "&index=" + bookIndex;

        R<String> r = get(url, new TypeReference<>() {
        });

        return r.getData();
    }

    /**
     * 获取章节目录列表
     *
     * @return 章节目录列表
     */
    public List<BookChapterDTO> getChapterList(String bookUrl) {
        // 调用API获取书架目录
        String url = USER_BEHAVIOR_PROPERTIES.getAddress() + AddressEnum.GET_CHAPTER_LIST.getAddress() + "?url=" + URLUtil.encodeAll(bookUrl);

        R<List<BookChapterDTO>> r = get(url, new TypeReference<>() {
        });

        return r.getData();
    }

    /**
     * 保存阅读进度
     */
    public void saveBookProgress(String author, String name, int index, String title, int durChapterPos) {
        // 调用API获取书架目录
        String url = USER_BEHAVIOR_PROPERTIES.getAddress() + AddressEnum.SAVE_BOOK_PROGRESS.getAddress();

        BookProgressDTO bookProgressDTO = BookProgressDTO.builder()
                .author(author)
                .name(name)
                .durChapterIndex(index)
                .durChapterTitle(title)
                .durChapterTime(System.currentTimeMillis())
                .durChapterPos(durChapterPos)
                .url(CurrentReadData.getBook().getBookUrl())
                .index(index)
                .build();

        post(url, bookProgressDTO, new TypeReference<>() {
        });
    }


    private <R> R get(String url, TypeReference<R> typeReference) {
        String textBody;
        try {
            textBody = HttpUtil.get(url, GLOBAL_SETTING_PROPERTIES.getApiCustomParam());
        } catch (Exception e) {
            throw new RuntimeException(String.format("\n%s：%s\n参数：\n%s\n", "调用API失败", url, GLOBAL_SETTING_PROPERTIES.getApiCustomParam()), e);
        }

        return JSONUtil.toBean(textBody, typeReference, true);
    }

    private <R> R post(String url, Object body, TypeReference<R> typeReference) {
        String textBody;
        try (HttpResponse execute = HttpUtil.createPost(url)
                .form(GLOBAL_SETTING_PROPERTIES.getApiCustomParam())
                .body(JSONUtil.toJsonStr(body))
                .execute()) {
            textBody = execute.body();
        } catch (Exception e) {
            throw new RuntimeException(String.format("\n%s：%s\n参数：\n%s\n%s\n", "调用API失败", url, GLOBAL_SETTING_PROPERTIES.getApiCustomParam(), body), e);
        }

        return JSONUtil.toBean(textBody, typeReference, true);
    }

}
