package com.tabnine.chat.commandHandlers.chatState

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnine.chat.commandHandlers.ChatMessageHandler

class GetChatStateHandler(gson: Gson) : ChatMessageHandler<Unit, ChatStateData>(gson) {
    private val chatState = ChatState(gson)

    override fun handle(payload: Unit?, project: Project): ChatStateData {
        return chatState.get()
    }

    override fun deserializeRequest(data: JsonElement?) {}
}
