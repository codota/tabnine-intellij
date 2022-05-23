package com.tabnine.inline.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.tabnine.inline.CompletionPreview

class InlineCaretListener : CaretListener {
    override fun caretPositionChanged(event: CaretEvent) {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return
        }
        CompletionPreview.getInstance(event.editor)?.let { preview ->
            if (preview.isCurrentlyDisplayingInlays) preview.clear()
        }
    }
}
