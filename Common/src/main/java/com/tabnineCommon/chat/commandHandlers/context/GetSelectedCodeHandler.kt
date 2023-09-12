package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.utils.ActionPermissions
import com.tabnineCommon.chat.commandHandlers.utils.AsyncAction

data class GetSelectedCodeResponsePayload(private val code: String, private val startLine: Int, private val endLine: Int)

class GetSelectedCodeHandler(gson: Gson) :
    ChatMessageHandler<Unit, GetSelectedCodeResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): GetSelectedCodeResponsePayload? {
        val editor = getEditorFromProject(project) ?: return null
        val selectedCode = AsyncAction(ActionPermissions.READ).execute {
            editor.selectionModel.selectedText
        }.join()

        if (selectedCode.isNullOrEmpty()) return null

        val selectionStartOffset = editor.selectionModel.selectionStart
        val selectionEndOffset = editor.selectionModel.selectionEnd
        val document = editor.document

        val startLine = document.getLineNumber(selectionStartOffset) + 1
        val endLine = document.getLineNumber(selectionEndOffset) + 1

        return GetSelectedCodeResponsePayload(selectedCode, startLine, endLine)
    }

    override fun deserializeRequest(data: JsonElement?) {}
}
