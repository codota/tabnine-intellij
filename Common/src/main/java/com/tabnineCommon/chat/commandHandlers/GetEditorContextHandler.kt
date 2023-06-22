package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

data class SelectedCode(val code: String, val filePath: String)

data class GetEditorContextResponsePayload(val fileCode: String, val selectedCode: String, val selectedCodeUsages: List<SelectedCode> = emptyList())

class GetEditorContextHandler(gson: Gson) : ChatMessageHandler<Unit, GetEditorContextResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): GetEditorContextResponsePayload {
        val editor = getEditorFromProject(project) ?: return GetEditorContextResponsePayload("", "")

        val fileCode = editor.document.text
        val selectedCode = editor.selectionModel.selectedText ?: ""

        return GetEditorContextResponsePayload(fileCode, selectedCode)
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}

private fun getEditorFromProject(project: Project): Editor? {
    val fileEditor = FileEditorManager.getInstance(project).selectedEditor ?: return null
    val dataContext = DataManager.getInstance().getDataContext(fileEditor.component)

    return CommonDataKeys.EDITOR.getData(dataContext)
}
