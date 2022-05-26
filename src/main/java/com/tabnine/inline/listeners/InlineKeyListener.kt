package com.tabnine.inline.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.tabnine.inline.CompletionPreview
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

private val INLINE_SHORTCUTS =
    setOf(KeyEvent.VK_ALT, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_TAB)

class InlineKeyListener(
    private val completionPreview: CompletionPreview
) : KeyAdapter(), Disposable {
    init {
        completionPreview.editor.contentComponent.addKeyListener(this)
        Disposer.register(completionPreview, this)
    }

    override fun keyReleased(event: KeyEvent) {
        if (INLINE_SHORTCUTS.contains(event.keyCode)) {
            return
        }

        Logger.getInstance(javaClass).warn("BOAZ: KeyListenere disposing completion. Event: " + event.toString())
        Disposer.dispose(completionPreview)
    }

    override fun dispose() {
        Logger.getInstance(javaClass).warn("BOAZ: KeyListenere disposed")
        completionPreview.editor.contentComponent.removeKeyListener(this)
    }
}
