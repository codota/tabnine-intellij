package com.tabnine.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager

data class SelectedCode(val code: String, val filePath: String)

data class GetEditorContextResponsePayload(
    val fileCode: String = "",
    val selectedCode: String = "",
    val selectedCodeUsages: List<SelectedCode> = emptyList(),
    val fileUri: String? = null,
    val language: String? = null,
    val lineTextAtCursor: String? = null,
)

class GetEditorContextHandler(gson: Gson) : ChatMessageHandler<Unit, GetEditorContextResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): GetEditorContextResponsePayload {
        val editor = getEditorFromProject(project) ?: return GetEditorContextResponsePayload()

        val fileCode = editor.document.text
        val selectedCode = editor.selectionModel.selectedText ?: ""
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        val fileUri = psiFile?.virtualFile?.path
        val language = psiFile?.language?.id

        val lineTextAtCursor = getLineAtCursor(editor, editor.caretModel.currentCaret.offset)

        return GetEditorContextResponsePayload(
            fileCode = fileCode,
            selectedCode = selectedCode,
            selectedCodeUsages = emptyList(),
            fileUri = fileUri,
            lineTextAtCursor = lineTextAtCursor,
            language = language
        )
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

    override fun deserializeRequest(data: JsonElement?) {}
}

private fun getEditorFromProject(project: Project): Editor? {
    val fileEditor = FileEditorManager.getInstance(project).selectedEditor ?: return null
    val dataContext = DataManager.getInstance().getDataContext(fileEditor.component)

    return CommonDataKeys.EDITOR.getData(dataContext)
}
