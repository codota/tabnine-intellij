package com.tabnineCommon.chat.commandHandlers.context

import com.intellij.openapi.editor.Editor
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction
import java.util.concurrent.CompletableFuture

data class EditorContext(
    private var fileCode: String = "",
    private var selectedCode: String = "",
    private var currentLineIndex: Int? = null,
) : EnrichingContextData {
    // Used for serialization - do not remove
    private val type: EnrichingContextType = EnrichingContextType.Editor

    private constructor(fileCode: String, selectedCode: String, currentLineIndex: Int) : this() {
        this.fileCode = fileCode
        this.selectedCode = selectedCode
        this.currentLineIndex = currentLineIndex
    }

    fun getSelectedCode(): String = selectedCode

    companion object {
        fun createFuture(editor: Editor): CompletableFuture<EditorContext> {
            return AsyncAction(ActionPermissions.READ).execute {
                create(editor)
            }
        }

        private fun create(editor: Editor): EditorContext {
            val fileCode = editor.document.text
            val selectedCode = editor.selectionModel.selectedText ?: ""
            val currentLineIndex = editor.caretModel.currentCaret.logicalPosition.line

            return EditorContext(fileCode, selectedCode, currentLineIndex)
        }
    }
}
