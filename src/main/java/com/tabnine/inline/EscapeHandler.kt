package com.tabnine.inline

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.tabnine.binary.requests.notifications.shown.SuggestionDroppedReason
import com.tabnine.general.DependencyContainer

class EscapeHandler(private val myOriginalHandler: EditorActionHandler) : EditorActionHandler() {
    private val completionsEventSender = DependencyContainer.instanceOfCompletionsEventSender()

    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val currentCompletion = CompletionPreview.getCurrentCompletion(editor)
        if (currentCompletion != null) {
            InlineCompletionCache.instance.clear(editor)
        }
        completionsEventSender.sendSuggestionDropped(
            editor,
            currentCompletion,
            SuggestionDroppedReason.ManualCancel
        )
        CompletionPreview.clear(editor)
        if (myOriginalHandler.isEnabled(editor, caret, dataContext)) {
            myOriginalHandler.execute(editor, caret, dataContext)
        }
    }

    public override fun isEnabledForCaret(
        editor: Editor,
        caret: Caret,
        dataContext: DataContext
    ): Boolean {
        val preview = CompletionPreview.getInstance(editor)
        return if (preview != null) {
            true
        } else myOriginalHandler.isEnabled(editor, caret, dataContext)
    }

    companion object {
        const val ACTION_ID = "EditorEscape"
    }
}
