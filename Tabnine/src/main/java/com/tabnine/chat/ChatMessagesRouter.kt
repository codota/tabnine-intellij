package com.tabnine.chat

import InitHandler
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnine.chat.commandHandlers.ChatMessageHandler
import com.tabnine.chat.commandHandlers.GetEditorContextHandler
import com.tabnine.chat.commandHandlers.GetUserHandler
import com.tabnine.chat.commandHandlers.SendEventHandler
import com.tabnine.chat.commandHandlers.chatState.ClearChatStateHandler
import com.tabnine.chat.commandHandlers.chatState.GetChatStateHandler
import com.tabnine.chat.commandHandlers.chatState.UpdateChatConversationHandler

data class ChatMessageRequest(val id: String, val command: String, val data: JsonElement? = null)
data class ChatMessageResponse(val id: String, val payload: Any? = null)

class ChatMessagesRouter {
    private val gson = GsonBuilder().create()
    private val commandHandlers = mapOf<String, ChatMessageHandler<*, *>>(
        "get_user" to GetUserHandler(gson),
        "send_event" to SendEventHandler(gson),
        "get_editor_context" to GetEditorContextHandler(gson),
        "update_chat_conversation" to UpdateChatConversationHandler(gson),
        "get_chat_state" to GetChatStateHandler(gson),
        "clear_all_chat_conversations" to ClearChatStateHandler(gson),
        "init" to InitHandler(gson),
    )

    fun handleRawMessage(rawRequest: String, project: Project): String {
        return gson.toJson(handleMessage(rawRequest, project))
    }

    private fun handleMessage(rawRequest: String, project: Project): ChatMessageResponse {
        val request = gson.fromJson(rawRequest, ChatMessageRequest::class.java)

        val commandHandler = commandHandlers[request.command] ?: return ChatMessageResponse(request.id)

        val responsePayload = commandHandler.handleRaw(request.data, project) ?: return ChatMessageResponse(request.id)

        return ChatMessageResponse(request.id, responsePayload)
    }
}
