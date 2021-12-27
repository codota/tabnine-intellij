package com.tabnine.inline.render.preserved

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import com.tabnine.inline.render.GraphicsUtils
import java.awt.Graphics
import java.awt.Rectangle

class InlayElementRenderer(private val editor: Editor, private val suffix: String, private val deprecated: Boolean) :
    EditorCustomElementRenderer {
    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return editor.contentComponent.getFontMetrics(GraphicsUtils.getFont(editor, deprecated)).stringWidth(suffix)
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        g.color = JBColor.GRAY
        g.font = GraphicsUtils.getFont(editor, deprecated)
        g.drawString(suffix, targetRegion.x, targetRegion.y + editor.ascent)
    }
}
