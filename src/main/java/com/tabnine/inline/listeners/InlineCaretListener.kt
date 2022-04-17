package com.tabnine.inline.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.util.TextRange
import com.tabnine.inline.CompletionPreview

class InlineCaretListener : CaretListener {
    override fun caretPositionChanged(event: CaretEvent) {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return
        }
        CompletionPreview.findCompletionPreview(event.editor)?.let { preview ->
            if (preview.isCurrentlyDisplayingInlays && !changeIsASingleCharacterTyping(event)) preview.clear()
        }
    }
}

private fun changeIsASingleCharacterTyping(event: CaretEvent): Boolean {
    val editor = event.editor
    val lineDiff = event.newPosition.line - event.oldPosition.line
    val charDiff = event.newPosition.column - event.oldPosition.column
    if (lineDiff != 0 || charDiff != 1) {
        return false
    }
    val textInsideChangedRange: String = editor.getDocument().getText(
        TextRange(event.oldPosition.column, event.newPosition.column)
    )
    return !textInsideChangedRange.trim().isEmpty()
}
