package com.tabnine.inline.render.experimental;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import static com.tabnine.inline.render.GraphicsUtils.getFont;
import static com.tabnine.inline.render.GraphicsUtils.getNiceContrastColor;

public class InlineElementRenderer implements EditorCustomElementRenderer {
    private final Editor editor;
    private final String suffix;
    private final boolean deprecated;
    private Integer renderingXAnchor;
    private Color color;

    public InlineElementRenderer(Editor editor, String suffix, boolean deprecated) {
        this.editor = editor;
        this.suffix = suffix;
        this.deprecated = deprecated;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        return this.editor.getContentComponent()
                .getFontMetrics(getFont(this.editor, this.deprecated)).stringWidth(this.suffix);
    }

    @Override
    public void paint(
            @NotNull Inlay inlay,
            @NotNull Graphics g,
            @NotNull Rectangle targetRegion,
            @NotNull TextAttributes textAttributes) {
        renderingXAnchor = renderingXAnchor == null ? targetRegion.x : renderingXAnchor;
        color = color == null ? getNiceContrastColor() : color;
        g.setColor(color);
        g.setFont(getFont(this.editor, this.deprecated));
        g.drawString(suffix, renderingXAnchor, targetRegion.y + editor.getAscent());
    }
}
