package com.tabnine.inline.render;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class GraphicsUtils {
    public static Font getFont(@NotNull Editor editor, boolean deprecated) {
        Font font = editor.getColorsScheme().getFont(EditorFontType.ITALIC);
        if (!deprecated) {
            return font;
        }

        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        return new Font(attributes);
    }

    public static Color getNiceContrastColor() {
        double averageBrightness = (getBrightness(JBColor.background()) + getBrightness(JBColor.foreground())) / 2.;
        Color currentResult = Color.lightGray;
        Color bestResult = currentResult;
        double currentBrightness = getBrightness(currentResult);
        double distance = Double.MAX_VALUE;
        double minBrightness = (getBrightness(Color.darkGray));

        while (currentBrightness > minBrightness) {
            if (Math.abs(currentBrightness - averageBrightness) < distance) {
                distance = Math.abs(currentBrightness - averageBrightness);
                bestResult = currentResult;
            }
            currentResult = currentResult.darker();
            currentBrightness = getBrightness(currentResult);
        }

        return bestResult;
    }

    static double getBrightness(Color color) {
        return Math.sqrt(
                (color.getRed() * color.getRed() * 0.241) +
                (color.getGreen() * color.getGreen() * 0.691) +
                (color.getBlue() * color.getBlue() * 0.068)
        );
    }
}
