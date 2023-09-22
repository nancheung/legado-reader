package com.nancheung.plugins.jetbrains.legadoreader.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JSONUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .registerModule(new AwtModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    public <T> T toBean(String jsonStr, Class<T> clazz) {
        return OBJECT_MAPPER.readValue(jsonStr, clazz);
    }

    @SneakyThrows
    public String toJsonStr(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }
}
