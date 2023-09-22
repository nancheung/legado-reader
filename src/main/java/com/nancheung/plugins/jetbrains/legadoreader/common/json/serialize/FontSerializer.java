package com.nancheung.plugins.jetbrains.legadoreader.common.json.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.awt.*;
import java.io.IOException;

public class FontSerializer extends JsonSerializer<Font> {
    @Override
    public void serialize(Font font, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        String name = font.getName();
        int style = font.getStyle();
        int size = font.getSize();
        jsonGenerator.writeString(String.format("%s,%d,%d", name, style, size));
    }

    @Override
    public Class<Font> handledType() {
        return Font.class;
    }
}
