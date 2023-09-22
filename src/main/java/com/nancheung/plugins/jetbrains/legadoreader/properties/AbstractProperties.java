package com.nancheung.plugins.jetbrains.legadoreader.properties;

import cn.hutool.core.bean.BeanUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.nancheung.plugins.jetbrains.legadoreader.common.Constant;
import com.nancheung.plugins.jetbrains.legadoreader.common.json.JSONUtils;
import com.nancheung.plugins.jetbrains.legadoreader.common.json.LogUtil;

/**
 * 配置抽象类
 *
 * @author NanCheung
 */
public abstract class AbstractProperties {

    private static final Logger log = Logger.getInstance(AbstractProperties.class);

    /**
     * 配置名称
     *
     * @return 配置名称
     */
    protected abstract String name();

    /**
     * 加载配置
     */
    protected void load() {
        String settingsStr = PropertiesComponent.getInstance().getValue(getName());

        AbstractProperties bean;
        try {
            bean = JSONUtils.toBean(settingsStr, this.getClass());
        } catch (Exception e) {
            LogUtil.error(log, "读取配置文件失败",e);
            return;
        }
        BeanUtil.copyProperties(bean, this);
    }

    /**
     * 保存配置
     */
    public void save() {
        String jsonStr;
        try {
            jsonStr = JSONUtils.toJsonStr(this);
        } catch (Exception e) {
            LogUtil.error(log, "保存配置文件失败",e);
            return;
        }

        PropertiesComponent.getInstance().setValue(getName(), jsonStr);
    }

    private String getName() {
        return Constant.PLUGIN_SETTING_ID + "." + name();
    }
}
