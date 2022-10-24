package com.tabnine.inline

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.tabnine.inline.render.getTabSize
import java.util.regex.Pattern

object CompletionUtils {
    private val END_OF_LINE_VALID_PATTERN = Pattern.compile("^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$")

    @JvmStatic
    fun isValidDocumentChange(editor: Editor, document: Document, newOffset: Int, previousOffset: Int): Boolean {
        if (newOffset < 0 || newOffset > previousOffset) return false

        val addedText = document.getText(TextRange(previousOffset, newOffset))
        return (
            isValidMidlinePosition(document, newOffset) &&
                isValidNonEmptyChange(addedText.length, addedText) &&
                isSingleCharNonWhitespaceChange(addedText) &&
                isNotIndentationChange(addedText, editor)
            )
    }

    @JvmStatic
    fun isValidMidlinePosition(document: Document, offset: Int): Boolean {
        val lineIndex: Int = document.getLineNumber(offset)
        val lineRange = TextRange.create(document.getLineStartOffset(lineIndex), document.getLineEndOffset(lineIndex))
        val line = document.getText(lineRange)
        val lineSuffix = line.substring(offset - lineRange.startOffset)
        return END_OF_LINE_VALID_PATTERN.matcher(lineSuffix).matches()
    }

    @JvmStatic
    fun isValidNonEmptyChange(replacedTextLength: Int, newText: String): Boolean {
        return replacedTextLength >= 0 && newText != ""
    }

    @JvmStatic
    fun isSingleCharNonWhitespaceChange(newText: String): Boolean {
        return newText.trim().length <= 1
    }

    @JvmStatic
    fun isNotIndentationChange(newText: String, editor: Editor): Boolean {
        return newText != getTabSize(editor)?.let { " ".repeat(it) }
    }
}
