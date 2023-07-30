package com.tabnineCommon.chat.commandHandlers.context

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange

data class SelectedCode(val code: String, val filePath: String)

data class EditorContext(
    private var fileCode: String = "",
    private var selectedCode: String = "",
    private var currentLineIndex: Int? = null,
    private val selectedCodeUsages: List<SelectedCode> = emptyList(),
    private var lineTextAtCursor: String? = null,
) : EnrichingContextData {
    private val type: EnrichingContextType = EnrichingContextType.Editor

    private constructor(fileCode: String, selectedCode: String, currentLineIndex: Int, lineTextAtCursor: String?) : this() {
        this.fileCode = fileCode
        this.selectedCode = selectedCode
        this.currentLineIndex = currentLineIndex
        this.lineTextAtCursor = lineTextAtCursor
    }

    companion object {
        fun create(editor: Editor): EditorContext {
            val fileCode = editor.document.text
            val selectedCode = editor.selectionModel.selectedText ?: ""
            val currentLineIndex = editor.caretModel.currentCaret.logicalPosition.line
            val lineTextAtCursor = getLineAtCursor(editor, editor.caretModel.currentCaret.offset)

            return EditorContext(fileCode, selectedCode, currentLineIndex, lineTextAtCursor)
        }

        private fun getLineAtCursor(editor: Editor, offset: Int): String? {
            return try {
                val lineNumber = editor.document.getLineNumber(offset)
                val lineStart = editor.document.getLineStartOffset(lineNumber)
                val lineEnd = editor.document.getLineEndOffset(lineNumber)

                editor.document.getText(TextRange(lineStart, lineEnd))
            } catch (e: Exception) {
                Logger.getInstance(EditorContext::class.java).warn("failed to get line at cursor", e)
                null
            }
        }
    }
}
