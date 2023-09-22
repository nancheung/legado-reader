package com.nancheung.plugins.jetbrains.legadoreader.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户行为配置
 *
 * @author erqian.zn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserBehaviorProperties extends AbstractProperties {

    /**
     * 当前服务器地址
     */
    private String address;

    /**
     * 历史address
     */
    private Map<String, LocalDateTime> addressHistory = new LinkedHashMap<>(4);

    public void addAddress(String addr) {
        address = addr;
        addressHistory.put(addr, LocalDateTime.now());

        // 只保留最近的4条记录
        if (addressHistory.size() > 4) {
            addressHistory = addressHistory.entrySet().stream()
                    .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                    .limit(4)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        save();
    }

    /**
     * 获取历史地址集合
     * <p>addressHistory的key转为list，按照value倒序</p>
     *
     * @return 历史地址集合, 最多4条
     * @see UserBehaviorProperties#addressHistory
     */
    public List<String> getAddressHistoryList() {
        return addressHistory.entrySet()
                .stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    protected String name() {
        return "user-behavior";
    }
}
