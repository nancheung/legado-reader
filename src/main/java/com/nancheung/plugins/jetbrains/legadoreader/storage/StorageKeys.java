package com.nancheung.plugins.jetbrains.legadoreader.storage;

/**
 * 存储键常量
 *
 * @author NanCheung
 */
public interface StorageKeys {
    /**
     * 命名空间
     */
    String NAMESPACE = "com.nancheung.legado-reader";

    /**
     * 插件设置存储键
     */
    String SETTINGS = NAMESPACE + ".settings";

    /**
     * 地址历史存储键
     */
    String ADDRESS_HISTORY = NAMESPACE + ".addressHistory";
}
