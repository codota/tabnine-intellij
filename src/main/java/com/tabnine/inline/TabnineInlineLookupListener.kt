package com.tabnine.inline

import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.tabnine.general.DependencyContainer

class TabnineInlineLookupListener : LookupListener {
    private val handler = DependencyContainer.singletonOfInlineCompletionHandler()

    override fun currentItemChanged(event: LookupEvent) {
        val eventItem = event.item
        if (!event.lookup.isFocused || eventItem == null) {
            return
        }

        val editor = event.lookup.editor
        val lastShownSuggestion = CompletionPreview.getCurrentCompletion(editor)
        CompletionPreview.clear(editor)
        InlineCompletionCache.instance.clear(editor)

        val userPrefix = event.lookup.itemPattern(eventItem)
        val completionInFocus = eventItem.lookupString

        // a weird case when the user presses ctrl+enter but the popup isn't rendered
        // (DocumentChanged event is triggered in this case)
        if (userPrefix == completionInFocus) {
            lastShownSuggestion?.let {
                handler.sendSuggestionDroppedEvent(editor, it)
            }
            return
        }

        if (!completionInFocus.startsWith(userPrefix)) {
            lastShownSuggestion?.let {
                handler.sendSuggestionDroppedEvent(editor, it)
            }
            return
        }

        handler.retrieveAndShowCompletion(
            editor,
            editor.caretModel.offset,
            lastShownSuggestion,
            "",
            LookAheadCompletionAdjustment(userPrefix, completionInFocus)
        )
    }

    override fun lookupCanceled(event: LookupEvent) {
        // Do nothing, but the validator is furious if we don't implement this.
        // Probably because in older versions this was not implemented.
    }

    override fun itemSelected(event: LookupEvent) {
        // Do nothing, but the validator is furious if we don't implement this.
        // Probably because in older versions this was not implemented.
    }
}
