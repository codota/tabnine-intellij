package com.tabnine.inline.render.preserved;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.tabnine.inline.render.FontFinder.getFont;

public class InlayElementRenderer implements EditorCustomElementRenderer {
    private final Editor editor;
    private final String suffix;
    private final boolean deprecated;

    public InlayElementRenderer(Editor editor, String suffix, boolean deprecated) {
        this.editor = editor;
        this.suffix = suffix;
        this.deprecated = deprecated;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        return editor.getContentComponent().getFontMetrics(getFont(editor, deprecated)).stringWidth(suffix);
    }

    @Override
    public void paint(
            @NotNull Inlay inlay,
            @NotNull Graphics g,
            @NotNull Rectangle targetRegion,
            @NotNull TextAttributes textAttributes) {
        g.setColor(JBColor.GRAY);
        g.setFont(getFont(editor, deprecated));
        g.drawString(suffix, targetRegion.x, targetRegion.y + editor.getAscent());
    }
}
