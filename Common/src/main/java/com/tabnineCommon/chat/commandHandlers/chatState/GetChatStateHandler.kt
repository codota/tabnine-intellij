package com.tabnineCommon.chat.commandHandlers.chatState

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler

class GetChatStateHandler(gson: Gson) : ChatMessageHandler<Unit, ChatStateData>(gson) {
    private val chatState = ChatState(gson)

    override fun handle(payload: Unit?, project: Project): ChatStateData {
        return chatState.get()
    }

    override fun deserialize(data: JsonElement?) {}
}
