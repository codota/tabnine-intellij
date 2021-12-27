package com.tabnine.inline.render.preserved

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.util.Disposer
import com.tabnine.inline.render.TabnineInlay
import com.tabnine.prediction.TabNineCompletion
import java.awt.Rectangle

class DefaultTabnineInlay : TabnineInlay {
    private var inlay: Inlay<*>? = null

    override val offset: Int?
        get() = inlay?.offset

    override val bounds: Rectangle?
        get() = inlay?.bounds

    override val isEmpty: Boolean
        get() = inlay == null

    override fun register(parent: Disposable) {
        inlay?.let {
            Disposer.register(parent, it)
        }
    }

    override fun clear() {
        inlay?.let {
            Disposer.dispose(it)
            inlay = null
        }
    }

    override fun render(editor: Editor, suffix: String, completion: TabNineCompletion, offset: Int) {
        val inlayElementRenderer = InlayElementRenderer(editor, suffix, completion.deprecated)
        inlay = editor
            .inlayModel
            .addInlineElement(offset, true, inlayElementRenderer)
    }
}
