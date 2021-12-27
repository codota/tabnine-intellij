package com.tabnine.inline.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.tabnine.inline.CompletionPreview

class InlineFocusListener : FocusChangeListener {
    override fun focusGained(editor: Editor) {}
    override fun focusLost(editor: Editor) {
        CompletionPreview.findCompletionPreview(editor)?.let { preview ->
            if (preview.isCurrentlyNotDisplayingInlays) preview.clear()
        }
    }
}
