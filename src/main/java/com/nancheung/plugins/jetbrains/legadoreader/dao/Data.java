package com.nancheung.plugins.jetbrains.legadoreader.dao;

import com.nancheung.plugins.jetbrains.legadoreader.api.dto.BookDTO;
import com.nancheung.plugins.jetbrains.legadoreader.manager.AddressHistoryManager;
import com.nancheung.plugins.jetbrains.legadoreader.manager.PluginSettingsManager;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据访问类（兼容层）
 * 代理到新的 Manager 层，保持向后兼容
 *
 * @author NanCheung
 * @deprecated 建议直接使用 {@link PluginSettingsManager} 和 {@link AddressHistoryManager}
 */
@UtilityClass
@Deprecated
public class Data {

    // ========== 插件设置相关（代理到 PluginSettingsManager） ==========

    /**
     * 正文字体颜色
     * @deprecated 使用 {@link PluginSettingsManager#getTextBodyFontColor()}
     */
    @Deprecated
    public Color textBodyFontColor;

    /**
     * 正文字体
     * @deprecated 使用 {@link PluginSettingsManager#getTextBodyFont()}
     */
    @Deprecated
    public Font textBodyFont;

    /**
     * API 自定义参数
     * @deprecated 使用 {@link PluginSettingsManager#getApiCustomParam()}
     */
    @Deprecated
    public Map<String, Object> apiCustomParam;

    /**
     * 是否启用错误日志
     * @deprecated 使用 {@link PluginSettingsManager#isEnableErrorLog()}
     */
    @Deprecated
    public boolean enableErrorLog;

    /**
     * 是否启用在代码行中显示正文
     */
    public boolean enableShowBodyInLine = false;

    // 静态代码块：初始化时从 Manager 同步数据到字段
    static {
        syncFromManagers();
    }

    /**
     * 从 Manager 同步数据到静态字段（保持向后兼容）
     */
    private void syncFromManagers() {
        PluginSettingsManager settingsManager = PluginSettingsManager.getInstance();
        textBodyFontColor = settingsManager.getTextBodyFontColor();
        textBodyFont = settingsManager.getTextBodyFont();
        apiCustomParam = settingsManager.getApiCustomParam();
        enableErrorLog = settingsManager.isEnableErrorLog();
    }

    // ========== 地址历史相关（代理到 AddressHistoryManager） ==========

    /**
     * 添加地址到历史记录
     *
     * @param address 地址
     * @deprecated 使用 {@link AddressHistoryManager#addAddress(String)}
     */
    @Deprecated
    public void addAddress(String address) {
        AddressHistoryManager.getInstance().addAddress(address);
    }

    /**
     * 获取当前地址（最近使用的地址）
     *
     * @return 当前地址
     * @deprecated 使用 {@link AddressHistoryManager#getMostRecent()}
     */
    @Deprecated
    public String getAddress() {
        return AddressHistoryManager.getInstance().getMostRecent();
    }

    /**
     * 获取地址历史列表
     *
     * @return 地址列表
     * @deprecated 使用 {@link AddressHistoryManager#getAddressList()}
     */
    @Deprecated
    public List<String> getAddressHistory() {
        return AddressHistoryManager.getInstance().getAddressList();
    }

    // ========== 书架数据（临时存储，不持久化） ==========

    /**
     * 书架目录（临时存储在内存）
     * key: author + "#" + name
     * value: 书籍信息
     */
    private Map<String, BookDTO> bookshelf;

    private final BiFunction<String, String, String> BOOK_MAP_KEY_FUNC = (author, name) -> author + "#" + name;

    /**
     * 获取书籍
     *
     * @param author 作者
     * @param name   书名
     * @return 书籍信息
     */
    public BookDTO getBook(String author, String name) {
        if (bookshelf == null) {
            return null;
        }
        return bookshelf.get(BOOK_MAP_KEY_FUNC.apply(author, name));
    }

    /**
     * 设置书架
     *
     * @param books 书籍列表
     */
    public void setBookshelf(List<BookDTO> books) {
        Data.bookshelf = books.stream()
                .collect(Collectors.toMap(
                        book -> BOOK_MAP_KEY_FUNC.apply(book.getAuthor(), book.getName()),
                        Function.identity()
                ));
    }

    /**
     * 设置 API 自定义参数（仅用于兼容，实际不需要调用）
     * 参数会在设置保存时自动处理
     *
     * @param apiCustomParamStr 参数字符串
     * @return 参数 Map
     * @deprecated 此方法不再需要，设置会自动管理
     */
    @Deprecated
    public Map<String, Object> setApiCustomParam(String apiCustomParamStr) {
        // 仅用于兼容，返回当前的参数
        return PluginSettingsManager.getInstance().getApiCustomParam();
    }
}
