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
        // do not interfere with inline shortcuts
        if (key == KeyEvent.VK_ALT ||
            key == KeyEvent.VK_OPEN_BRACKET ||
            key == KeyEvent.VK_CLOSE_BRACKET ||
            key == KeyEvent.VK_TAB
        ) return
        preview.clear()
    }
}
