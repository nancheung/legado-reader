package com.nancheung.plugins.jetbrains.legadoreader.common.json.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.awt.*;
import java.io.IOException;

public class ColorDeserializer extends JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return new Color(p.getIntValue());
    }

    @Override
    public Class<Color> handledType() {
        return Color.class;
    }
}
