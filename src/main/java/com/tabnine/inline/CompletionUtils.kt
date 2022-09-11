package com.tabnine.inline

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

object CompletionUtils {
    private val END_OF_LINE_VALID_PATTERN = Pattern.compile("^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$")

    @JvmStatic
    fun isValidMidlinePosition(document: Document, offset: Int): Boolean {
        val lineIndex: Int = document.getLineNumber(offset)
        val lineRange = TextRange.create(document.getLineStartOffset(lineIndex), document.getLineEndOffset(lineIndex))
        val line = document.getText(lineRange)
        val lineSuffix = line.substring(offset - lineRange.startOffset)
        return END_OF_LINE_VALID_PATTERN.matcher(lineSuffix).matches()
    }
}
