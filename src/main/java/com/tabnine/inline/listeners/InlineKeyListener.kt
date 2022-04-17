package com.tabnine.inline.listeners

import com.intellij.openapi.editor.Editor
import com.tabnine.inline.CompletionPreview
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class InlineKeyListener(private val editor: Editor) : KeyAdapter() {
    override fun keyReleased(event: KeyEvent) {
        val preview = CompletionPreview.findCompletionPreview(editor) ?: return
        if (!preview.isCurrentlyDisplayingInlays) return

        val key = event.keyCode
        if (key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_DELETE)
            preview.clear()
    }
}
