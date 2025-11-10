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
import com.nancheung.plugins.jetbrains.legadoreader.manager.AddressHistoryManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.ReadingSessionManager;
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
        String url = AddressHistoryManager.getInstance().getMostRecent() + AddressEnum.GET_BOOKSHELF.getAddress();

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
        String url = AddressHistoryManager.getInstance().getMostRecent() + AddressEnum.GET_BOOK_CONTENT.getAddress() + "?url=" + URLUtil.encodeAll(bookUrl) + "&index=" + bookIndex;

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
        String url = AddressHistoryManager.getInstance().getMostRecent() + AddressEnum.GET_CHAPTER_LIST.getAddress() + "?url=" + URLUtil.encodeAll(bookUrl);

        R<List<BookChapterDTO>> r = get(url, new TypeReference<>() {
        });

        return r.getData();
    }

    /**
     * 保存阅读进度
     */
    public void saveBookProgress(String author, String name, int index, String title, int durChapterPos) {
        // 调用API获取书架目录
        String url = AddressHistoryManager.getInstance().getMostRecent() + AddressEnum.SAVE_BOOK_PROGRESS.getAddress();

        BookProgressDTO bookProgressDTO = BookProgressDTO.builder()
                .author(author)
                .name(name)
                .durChapterIndex(index)
                .durChapterTitle(title)
                .durChapterTime(System.currentTimeMillis())
                .durChapterPos(durChapterPos)
                .url(ReadingSessionManager.getInstance().getCurrentBook().getBookUrl())
                .index(index)
                .build();

        post(url, bookProgressDTO, new TypeReference<>() {
        });
    }


    private <R> R get(String url, TypeReference<R> typeReference) {
        String textBody;

        try {
            textBody = HttpUtil.get(url, PluginSettingsManager.getInstance().getApiCustomParam());
        } catch (Exception e) {
            throw new RuntimeException(String.format("\n%s：%s\n参数：\n%s\n", "调用API失败", url, PluginSettingsManager.getInstance().getApiCustomParam()), e);
        }

        return JSONUtil.toBean(textBody, typeReference, true);
    }

    private <R> R post(String url, Object body, TypeReference<R> typeReference) {
        String textBody;
        try (HttpResponse execute = HttpUtil.createPost(url)
                .form(PluginSettingsManager.getInstance().getApiCustomParam())
                .body(JSONUtil.toJsonStr(body))
                .execute()) {
            textBody = execute.body();
        } catch (Exception e) {
            throw new RuntimeException(String.format("\n%s：%s\n参数：\n%s\n%s\n", "调用API失败", url, PluginSettingsManager.getInstance().getApiCustomParam(), body), e);
        }

        return JSONUtil.toBean(textBody, typeReference, true);
    }

}
