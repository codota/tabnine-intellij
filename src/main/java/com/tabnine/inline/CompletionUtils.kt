package com.tabnine.inline

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

object CompletionUtils {
    private val END_OF_LINE_PATTERN = Pattern.compile("^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$")

    @JvmStatic
    fun isValidMiddleOfLinePosition(document: Document, offset: Int): Boolean {
        val lineNumber = document.getLineNumber(offset)
        val lineRange = TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber))
        val line = document.getText(lineRange)
        val lineEnd = line.substring(offset - lineRange.startOffset)
        return END_OF_LINE_PATTERN.matcher(lineEnd.trim()).matches()
    }
}
