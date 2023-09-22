package com.nancheung.plugins.jetbrains.legadoreader.common.json.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.io.IOException;

public class ColorSerializer extends JsonSerializer<Color> {
    @Override
    public void serialize(Color color, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        int rgb = color.getRGB();
        jsonGenerator.writeNumber(rgb);
    }

    @Override
    public Class<Color> handledType() {
        return Color.class;
    }
}
