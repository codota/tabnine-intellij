package com.tabnine.chat.commandHandlers.chatState

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnine.chat.commandHandlers.ChatMessageHandler

class ClearChatStateHandler(gson: Gson) : ChatMessageHandler<Unit, Unit>(gson) {
    private val chatState = ChatState(gson)

    override fun handle(payload: Unit?, project: Project) {
        chatState.clear()
    }

    override fun deserializeRequest(data: JsonElement?) {}
}
