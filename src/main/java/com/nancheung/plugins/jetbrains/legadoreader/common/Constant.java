package com.nancheung.plugins.jetbrains.legadoreader.common;

public interface Constant {
    /**
     * 插件包名
     */
    String PLUGIN_PACKAGE = "com.nancheung.plugins.jetbrains.legado-reader";
    /**
     * 插件id前缀
     */
    String PLUGIN_ID_PREFIX = "com.nancheung.legado-reader";
    /**
     * 插件设置id
     */
    String PLUGIN__SETTING_PREFIX = PLUGIN_ID_PREFIX + ".setting";
    /**
     * 插件设置id
     */
    String PLUGIN__SETTING_ID = PLUGIN__SETTING_PREFIX + ".id";
    /**
     * 持久化数据
     */
    String PLUGIN__PERSISTENCE_DATA = PLUGIN_ID_PREFIX + ".persistence.data";
}
