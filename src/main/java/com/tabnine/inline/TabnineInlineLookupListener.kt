package com.tabnine.inline

import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.tabnine.binary.requests.notifications.shown.SuggestionDroppedReason
import com.tabnine.general.DependencyContainer

class TabnineInlineLookupListener : LookupListener {
    private val handler = DependencyContainer.singletonOfInlineCompletionHandler()
    private val completionsEventSender = DependencyContainer.instanceOfCompletionsEventSender()

    override fun currentItemChanged(event: LookupEvent) {
        val editor = event.lookup.editor
        completionsEventSender.sendSuggestionDropped(
            editor,
            CompletionPreview.getCurrentCompletion(editor),
            SuggestionDroppedReason.ManualCancel
        )
        CompletionPreview.clear(editor)
    }

    override fun lookupCanceled(event: LookupEvent) {
        val editor = event.lookup.editor
        handler.retrieveAndShowCompletion(editor, editor.caretModel.offset, null, "", DefaultCompletionAdjustment(), false)
    }

    override fun itemSelected(event: LookupEvent) {
        // Do nothing, but the validator is furious if we don't implement this.
        // Probably because in older versions this was not implemented.
    }
}
