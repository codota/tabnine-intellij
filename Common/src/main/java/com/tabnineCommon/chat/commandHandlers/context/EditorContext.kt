package com.tabnineCommon.chat.commandHandlers.context

import com.intellij.openapi.editor.Editor
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction
import java.util.concurrent.CompletableFuture

data class EditorContext(
    private var fileCode: String = "",
    private var currentLineIndex: Int? = null,
) : EnrichingContextData {
    // Used for serialization - do not remove
    private val type: EnrichingContextType = EnrichingContextType.Editor

    private constructor(fileCode: String, currentLineIndex: Int) : this() {
        this.fileCode = fileCode
        this.currentLineIndex = currentLineIndex
    }

    companion object {
        fun createFuture(editor: Editor): CompletableFuture<EditorContext> {
            return AsyncAction(ActionPermissions.READ).execute {
                create(editor)
            }
        }

        private fun create(editor: Editor): EditorContext {
            val fileCode = editor.document.text
            val currentLineIndex = editor.caretModel.currentCaret.logicalPosition.line

            return EditorContext(fileCode, currentLineIndex)
        }
    }
}
