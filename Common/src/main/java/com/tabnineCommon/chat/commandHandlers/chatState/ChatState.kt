package com.tabnineCommon.chat.commandHandlers.chatState

import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent

private const val CHAT_CONVERSATIONS_KEY = "com.tabnine.chat.conversations.v2"

data class ChatMessageProps(val id: String, val text: Any, val isBot: Boolean, val timestamp: String, val selected: Int?)
data class ChatConversation(val id: String, val messages: List<ChatMessageProps>)

data class ChatStateData(val conversations: MutableMap<String, ChatConversation>)

class ChatState(private val gson: Gson) {
    fun save(conversation: ChatConversation) {
        val stateRaw = PropertiesComponent.getInstance().getValue(CHAT_CONVERSATIONS_KEY)
        val state = stateRaw?.let { gson.fromJson(it, ChatStateData::class.java) } ?: ChatStateData(mutableMapOf())

        state.conversations[conversation.id] = conversation

        PropertiesComponent.getInstance().setValue(CHAT_CONVERSATIONS_KEY, gson.toJson(state))
    }

    fun get(): ChatStateData {
        return PropertiesComponent.getInstance().getValue(CHAT_CONVERSATIONS_KEY)?.let { gson.fromJson(it, ChatStateData::class.java) } ?: ChatStateData(mutableMapOf())
    }

    fun clear() {
        PropertiesComponent.getInstance().unsetValue(CHAT_CONVERSATIONS_KEY)
    }
}
