package com.nancheung.plugins.jetbrains.legadoreader.common.json;

import com.intellij.openapi.diagnostic.Logger;
import com.nancheung.plugins.jetbrains.legadoreader.properties.PropertiesFactory;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LogUtil {

    public void error(Logger log, String message, Throwable t) {
        if (PropertiesFactory.getGlobalSetting().isEnableErrorLog()) {
            log.error(message, t.getCause());
        }
    }

    public void info(Logger log, String message) {
        log.info(message);
    }
}
