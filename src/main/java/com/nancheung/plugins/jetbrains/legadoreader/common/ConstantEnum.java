package com.nancheung.plugins.jetbrains.legadoreader.common;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ConstantEnum {
    /**
     * 插件包名
     */
    PLUGIN_PACKAGE("com.nancheung.plugins.jetbrains.legado-reader"),
    /**
     * 插件id前缀
     */
    PLUGIN_ID_PREFIX("com.nancheung.legado-reader"),
    /**
     * 插件设置id
     */
    PLUGIN__SETTING_PREFIX(PLUGIN_ID_PREFIX.value + ".setting"),
    /**
     * 插件设置id
     */
    PLUGIN__SETTING_ID(PLUGIN__SETTING_PREFIX.value + ".setting.id"),
    ;
    
    private String value;
}
