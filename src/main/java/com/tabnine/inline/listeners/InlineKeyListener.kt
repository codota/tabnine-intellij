package com.tabnine.inline.listeners

import com.intellij.openapi.editor.Editor
import com.tabnine.inline.CompletionPreview
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

private val INLINE_SHORTCUTS =
    setOf(KeyEvent.VK_ALT, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_TAB)

class InlineKeyListener(private val editor: Editor) : KeyAdapter() {
    override fun keyReleased(event: KeyEvent) {
        if (INLINE_SHORTCUTS.contains(event.keyCode)) {
            return
        }

        CompletionPreview.getInstance(editor)?.clear()
    }
}
