package com.tabnineCommon.chat.commandHandlers.chatState

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler

class UpdateChatConversationHandler(gson: Gson) : ChatMessageHandler<ChatConversation, Unit>(gson) {
    private val chatState = ChatState(gson)

    override fun handle(payload: ChatConversation?, project: Project) {
        payload?.let { chatState.save(it) }
    }

    override fun deserializeRequest(data: JsonElement?): ChatConversation? {
        return gson.fromJson(data, ChatConversation::class.java)
    }
}
