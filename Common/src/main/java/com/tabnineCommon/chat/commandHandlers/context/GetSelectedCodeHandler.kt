package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler

data class GetSelectedCodeResponsePayload(private val code: String)

class GetSelectedCodeHandler(gson: Gson) :
    ChatMessageHandler<Unit, GetSelectedCodeResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): GetSelectedCodeResponsePayload? {
        val editor = getEditorFromProject(project) ?: return null
        val selectedCode = EditorContext.createFuture(editor).join().getSelectedCode()
        return if (selectedCode.isNotEmpty()) GetSelectedCodeResponsePayload(selectedCode) else null
    }

    override fun deserializeRequest(data: JsonElement?) {}
}
