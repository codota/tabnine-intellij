package com.tabnine.inline.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.util.Disposer
import com.intellij.util.ObjectUtils
import com.tabnine.inline.CompletionPreview

class InlineFocusListener(private val completionPreview: CompletionPreview) : FocusChangeListener {
    init {
        ObjectUtils.consumeIfCast(
            completionPreview.editor, EditorEx::class.java
        ) { e: EditorEx -> e.addFocusListener(this, completionPreview) }
    }

    override fun focusGained(editor: Editor) {}
    override fun focusLost(editor: Editor) {
        Disposer.dispose(completionPreview)
    }
}
