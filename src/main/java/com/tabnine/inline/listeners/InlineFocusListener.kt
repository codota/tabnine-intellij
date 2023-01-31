package com.tabnine.inline.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Disposer
import com.intellij.util.ObjectUtils
import com.tabnine.binary.requests.notifications.shown.SuggestionDroppedReason
import com.tabnine.general.DependencyContainer
import com.tabnine.inline.CompletionPreview
import com.tabnine.prediction.CompletionFacade

class InlineFocusListener(private val completionPreview: CompletionPreview) : FocusChangeListener {
    private val completionsEventSender = DependencyContainer.instanceOfCompletionsEventSender()
    init {
        ObjectUtils.consumeIfCast(
            completionPreview.editor, EditorEx::class.java
        ) { e: EditorEx -> e.addFocusListener(this, completionPreview) }
    }

    override fun focusGained(editor: Editor) {}
    override fun focusLost(editor: Editor) {
        val lastShownSuggestion = completionPreview.currentCompletion
        if (lastShownSuggestion != null) {
            try {
                val filename = CompletionFacade.getFilename(FileDocumentManager.getInstance().getFile(completionPreview.editor.document))

                completionsEventSender.sendSuggestionDropped(
                    lastShownSuggestion.netLength,
                    filename,
                    SuggestionDroppedReason.FocusChanged,
                    lastShownSuggestion.completionMetadata
                )
            } catch (e: Throwable) {
                Logger.getInstance(javaClass).warn("Focus listener failed to send suggestion dropped event", e)
            }
        }
        Disposer.dispose(completionPreview)
    }
}
