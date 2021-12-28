package com.tabnine.inline.render.experimental

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.tabnine.inline.render.GraphicsUtils
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

class InlineElementRenderer(private val editor: Editor, private val suffix: String, private val deprecated: Boolean) :
    EditorCustomElementRenderer {
    private var renderingXAnchor: Int? = null
    private var color: Color? = null
    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return editor.contentComponent
            .getFontMetrics(GraphicsUtils.getFont(editor, deprecated)).stringWidth(suffix)
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        renderingXAnchor = renderingXAnchor ?: targetRegion.x
        color = color ?: GraphicsUtils.niceContrastColor
        g.color = color
        g.font = GraphicsUtils.getFont(editor, deprecated)
        g.drawString(suffix, renderingXAnchor!!, targetRegion.y + editor.ascent)
    }
}
