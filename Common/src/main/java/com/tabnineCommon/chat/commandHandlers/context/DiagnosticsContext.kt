package com.tabnineCommon.chat.commandHandlers.context

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.util.Processor
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction
import java.awt.Point
import java.util.concurrent.CompletableFuture

data class DiagnosticItem(val errorMessage: String, val lineNumber: Int, val lineCode: String) {
    constructor(highlightInfo: HighlightInfo, editor: Editor) : this(
        errorMessage = highlightInfo.description,
        lineNumber = editor.document.getLineNumber(highlightInfo.actualStartOffset) + 1,
        lineCode = editor.document.getText(
            TextRange(
                editor.document.getLineStartOffset(editor.document.getLineNumber(highlightInfo.actualStartOffset)),
                editor.document.getLineEndOffset(editor.document.getLineNumber(highlightInfo.actualStartOffset))
            )
        ).trim()
    )
}

data class DiagnosticsContext(
    private val diagnostics: List<DiagnosticItem>? = null,
) : EnrichingContextData {
    // Used for serialization - do not remove
    private val type: EnrichingContextType = EnrichingContextType.Diagnostics

    companion object {
        fun createFuture(editor: Editor, project: Project): CompletableFuture<DiagnosticsContext?> {
            return AsyncAction(ActionPermissions.WRITE).execute {
                create(editor, project)
            }
        }

        private fun create(editor: Editor, project: Project): DiagnosticsContext? {
            val rangeToCheck: TextRange = getSelectedRange(editor) ?: getVisibleRange(editor) ?: return null
            val diagnostics = mutableListOf<DiagnosticItem>()

            DaemonCodeAnalyzerImpl.processHighlights(
                editor.document,
                project,
                HighlightSeverity.WARNING,
                rangeToCheck.startOffset,
                rangeToCheck.endOffset,
                Processor { highlightInfo ->
                    diagnostics.add(DiagnosticItem(highlightInfo, editor))
                }
            )

            Logger.getInstance(DiagnosticsContext::class.java).debug("diagnostics: $diagnostics")

            return DiagnosticsContext(diagnostics)
        }

        private fun getVisibleRange(editor: Editor): TextRange? {
            val visibleArea = editor.scrollingModel.visibleArea
            val upperLeft = visibleArea.location
            val bottomRight = Point(visibleArea.x + visibleArea.width, visibleArea.y + visibleArea.height)

            val startOffset = editor.logicalPositionToOffset(editor.xyToLogicalPosition(upperLeft))
            val endOffset = editor.logicalPositionToOffset(editor.xyToLogicalPosition(bottomRight))

            return try {
                TextRange(startOffset, endOffset)
            } catch (e: IllegalArgumentException) {
                Logger.getInstance(DiagnosticsContext::class.java).warn("failed to get visible range", e)
                null
            }
        }

        private fun getSelectedRange(editor: Editor): TextRange? {
            val selectionModel = editor.selectionModel
            return if (selectionModel.hasSelection()) {
                TextRange(selectionModel.selectionStart, selectionModel.selectionEnd)
            } else {
                null
            }
        }
    }
}
