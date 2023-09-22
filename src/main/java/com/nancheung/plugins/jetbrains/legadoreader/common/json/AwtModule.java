package com.nancheung.plugins.jetbrains.legadoreader.common.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.intellij.ui.JBColor;
import com.nancheung.plugins.jetbrains.legadoreader.common.json.deserializer.ColorDeserializer;
import com.nancheung.plugins.jetbrains.legadoreader.common.json.deserializer.FontDeserializer;
import com.nancheung.plugins.jetbrains.legadoreader.common.json.deserializer.JBColorDeserializer;
import com.nancheung.plugins.jetbrains.legadoreader.common.json.serialize.ColorSerializer;
import com.nancheung.plugins.jetbrains.legadoreader.common.json.serialize.FontSerializer;

import java.awt.*;

public class AwtModule extends SimpleModule {
    public AwtModule() {
        addSerializer(new ColorSerializer());
        addSerializer(new FontSerializer());

        addDeserializer(Color.class, new ColorDeserializer());
        addDeserializer(JBColor.class, new JBColorDeserializer());
        addDeserializer(Font.class, new FontDeserializer());
    }
}
