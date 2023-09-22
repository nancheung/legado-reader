package com.nancheung.plugins.jetbrains.legadoreader.common.json.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.io.IOException;

public class JBColorDeserializer extends JsonDeserializer<JBColor> {
    @Override
    public JBColor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        int rgb = p.getIntValue();
        return new JBColor(rgb, rgb);
    }

    @Override
    public Class<JBColor> handledType() {
        return JBColor.class;
    }
}
