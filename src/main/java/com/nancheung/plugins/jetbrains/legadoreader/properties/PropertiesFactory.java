package com.nancheung.plugins.jetbrains.legadoreader.properties;

import lombok.experimental.UtilityClass;

/**
 * 配置工厂
 *
 * @author NanCheung
 */
@UtilityClass
public class PropertiesFactory {
    private final GlobalSettingProperties GLOBAL_SETTING_PROPERTIES = new GlobalSettingProperties();
    private final UserBehaviorProperties USER_BEHAVIOR_PROPERTIES = new UserBehaviorProperties();

    static {
        GLOBAL_SETTING_PROPERTIES.load();
        USER_BEHAVIOR_PROPERTIES.load();
    }

    public GlobalSettingProperties getGlobalSetting() {
        return GLOBAL_SETTING_PROPERTIES;
    }

    public UserBehaviorProperties getUserBehavior() {
        return USER_BEHAVIOR_PROPERTIES;
    }
}
