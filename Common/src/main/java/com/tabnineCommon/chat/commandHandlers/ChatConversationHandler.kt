package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

private val CHAT_CONVERSATIONS_KEY = "com.tabnine.chat.conversations"

data class ChatMessageProps(val text: String, val isBot: Boolean, val timestamp: String)
data class ChatConversationRequestPayload(val id: String, val messages: List<ChatMessageProps>)

class UpdateChatConversationHandler(gson: Gson) : ChatMessageHandler<ChatConversationRequestPayload, EmptyPayload>(gson) {
    override fun handle(payload: ChatConversationRequestPayload?, project: Project): EmptyPayload? {
        if (payload == null) return null

        val chatConversationsRaw = PropertiesComponent.getInstance(project).getValue(CHAT_CONVERSATIONS_KEY, "{}")
        val chatConversations = gson.fromJson(chatConversationsRaw, Map::class.java)
        return null
    }

    override fun deserialize(data: JsonElement?): ChatConversationRequestPayload? {
        return gson.fromJson(data, ChatConversationRequestPayload::class.java)
    }
}
