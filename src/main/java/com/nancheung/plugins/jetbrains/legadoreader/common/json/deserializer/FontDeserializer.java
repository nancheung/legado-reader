package com.nancheung.plugins.jetbrains.legadoreader.common.json.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.awt.*;
import java.io.IOException;

public class FontDeserializer extends JsonDeserializer<Font> {
    @Override
    public Font deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String[] fontParts = p.getValueAsString().split(",");

        return new Font(fontParts[0], Integer.parseInt(fontParts[1]), Integer.parseInt(fontParts[2]));
    }

    @Override
    public Class<Font> handledType() {
        return Font.class;
    }
}
