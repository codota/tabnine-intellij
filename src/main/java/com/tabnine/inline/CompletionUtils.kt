package com.tabnine.inline

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

object CompletionUtils {
    private val END_OF_LINE_PATTERN = Pattern.compile("^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$")

    @JvmStatic
    fun isValidMiddleOfLinePosition(editor: Editor): Boolean {
        val lineEndOffset = editor.document.getLineEndOffset(editor.caretModel.logicalPosition.line)
        val lineEnd = editor.document.getText(TextRange(editor.caretModel.offset, lineEndOffset))
        return END_OF_LINE_PATTERN.matcher(lineEnd).matches()
    }
}
