package com.tabnine.inline.render;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class FontFinder {
    public static Font getFont(@NotNull Editor editor, boolean deprecated) {
        Font font = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        if (!deprecated) {
            return font;
        }
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        return new Font(attributes);
    }
}
