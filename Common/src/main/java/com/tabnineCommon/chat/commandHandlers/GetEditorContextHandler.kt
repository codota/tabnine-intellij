package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.Processor
import com.tabnineCommon.binary.requests.fileMetadata.FileMetadataRequest
import com.tabnineCommon.chat.commandHandlers.utils.SymbolsResolver
import com.tabnineCommon.chat.commandHandlers.utils.executeReadActionWithTimeout
import com.tabnineCommon.general.DependencyContainer
import java.awt.Point
import java.io.File
import java.util.concurrent.TimeUnit

data class EditorContextRequestPayload(val userQuery: String)

data class SelectedCode(val code: String, val filePath: String)

data class GetEditorContextResponsePayload(
    private val fileCode: String = "",
    private val selectedCode: String = "",
    private val selectedCodeUsages: List<SelectedCode> = emptyList(),
    private val diagnosticsText: String? = null,
    private val fileUri: String? = null,
    private val language: String? = null,
    private val lineTextAtCursor: String? = null,
    private var metadata: JsonObject? = null,
) {
    constructor(metadata: JsonObject?) : this() {
        this.metadata = metadata
    }
}

class GetEditorContextHandler(gson: Gson) :
    ChatMessageHandler<EditorContextRequestPayload, GetEditorContextResponsePayload>(gson) {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
    private val wordsRegex = Regex("\\b\\w+\\b")

    override fun handle(payload: EditorContextRequestPayload?, project: Project): GetEditorContextResponsePayload {
        val editor = getEditorFromProject(project) ?: return noEditorResponse(project)

        val fileCode = editor.document.text
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        val fileUri = psiFile?.virtualFile?.path
        val language = psiFile?.language?.id
        val selectedCode = getSelectedCode(project, editor, fileUri, payload) ?: ""

        val lineTextAtCursor = getLineAtCursor(editor, editor.caretModel.currentCaret.offset)
        val diagnosticsText = getDiagnosticsText(project, editor)

        var metadata = if (fileUri != null) binaryRequestFacade.executeRequest(FileMetadataRequest(fileUri)) else null

        if (metadata?.has("error") == true) {
            metadata = null
        }

        return GetEditorContextResponsePayload(
            fileCode = fileCode,
            selectedCode = selectedCode,
            selectedCodeUsages = emptyList(),
            diagnosticsText = diagnosticsText,
            fileUri = fileUri,
            lineTextAtCursor = lineTextAtCursor,
            language = language,
            metadata = metadata
        )
    }

    private fun getSelectedCode(
        project: Project,
        editor: Editor,
        fileUri: String?,
        payload: EditorContextRequestPayload?
    ): String? {
        val editorSelection = editor.selectionModel.selectedText
        if (!editorSelection.isNullOrBlank() || (payload ?: return null).userQuery.isBlank()) {
            return editorSelection
        }
        Logger.getInstance(javaClass).debug("searching symbols for selection")
        val wordsInQuery = wordsRegex.findAll(payload.userQuery).map { it.value }
        return try {
            val resolveAllSymbols = {
                wordsInQuery
                    .flatMap { word ->
                        SymbolsResolver.resolveSymbols(project, editor.document, word, 10)
                    }
                    .find { symbol -> symbol.absolutePath == fileUri }
                    ?.text
            }
            executeReadActionWithTimeout(resolveAllSymbols, 1, TimeUnit.SECONDS)
        } catch (e: Throwable) {
            Logger.getInstance(javaClass).warn("failed to resolve symbols for selection", e)
            null
        }
    }

    private fun noEditorResponse(project: Project): GetEditorContextResponsePayload {
        val firstFileInProject = project.basePath?.let { basePath -> File(basePath).walk().find { it.isFile } }
        var metadata =
            if (firstFileInProject != null) binaryRequestFacade.executeRequest(FileMetadataRequest(firstFileInProject.path)) else null

        if (metadata?.has("error") == true) {
            metadata = null
        }

        return GetEditorContextResponsePayload(metadata)
    }

    private fun getLineAtCursor(editor: Editor, offset: Int): String? {
        return try {
            val lineNumber = editor.document.getLineNumber(offset)
            val lineStart = editor.document.getLineStartOffset(lineNumber)
            val lineEnd = editor.document.getLineEndOffset(lineNumber)

            editor.document.getText(TextRange(lineStart, lineEnd))
        } catch (e: Exception) {
            Logger.getInstance(javaClass).warn("failed to get line at cursor", e)
            null
        }
    }

    override fun deserializeRequest(data: JsonElement?): EditorContextRequestPayload? {
        return gson.fromJson(data, EditorContextRequestPayload::class.java)
    }

    private fun getDiagnosticsText(project: Project, editor: Editor): String? {
        val visibleRange = getVisibleRange(editor) ?: return null
        val highlights = mutableListOf<String>()

        DaemonCodeAnalyzerImpl.processHighlights(
            editor.document,
            project,
            HighlightSeverity.WARNING,
            visibleRange.startOffset,
            visibleRange.endOffset,
            Processor { highlights.add(it.description) }
        )

        val diagnostics = highlights.joinToString("\n")
        Logger.getInstance(javaClass).debug("diagnostics: $diagnostics")
        return diagnostics
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
            Logger.getInstance(javaClass).warn("failed to get visible range", e)
            null
        }
    }
}
