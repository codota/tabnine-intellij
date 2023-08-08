package com.tabnineCommon.chat

import InitHandler
import InsertAtCursorHandler
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.GetUserHandler
import com.tabnineCommon.chat.commandHandlers.SendEventHandler
import com.tabnineCommon.chat.commandHandlers.chatSettings.GetChatSettingsHandler
import com.tabnineCommon.chat.commandHandlers.chatSettings.UpdateChatSettingsHandler
import com.tabnineCommon.chat.commandHandlers.chatState.ClearChatStateHandler
import com.tabnineCommon.chat.commandHandlers.chatState.GetChatStateHandler
import com.tabnineCommon.chat.commandHandlers.chatState.UpdateChatConversationHandler
import com.tabnineCommon.chat.commandHandlers.context.GetBasicContextHandler
import com.tabnineCommon.chat.commandHandlers.context.GetEnrichingContextHandler
import com.tabnineCommon.general.DependencyContainer

data class ChatMessageRequest(val id: String, val command: String, val data: JsonElement? = null)
data class ChatMessageResponse(val id: String, val payload: Any? = null, val error: String? = null)

class ChatMessagesRouter {
    private val gson = DependencyContainer.instanceOfGson()
    private val commandHandlers = mapOf<String, ChatMessageHandler<*, *>>(
        "init" to InitHandler(gson),
        "get_user" to GetUserHandler(gson),
        "send_event" to SendEventHandler(gson),
        "get_basic_context" to GetBasicContextHandler(gson),
        "get_enriching_context" to GetEnrichingContextHandler(gson),
        "update_chat_conversation" to UpdateChatConversationHandler(gson),
        "get_chat_state" to GetChatStateHandler(gson),
        "clear_all_chat_conversations" to ClearChatStateHandler(gson),
        "insert-at-cursor" to InsertAtCursorHandler(gson),
        "get_settings" to GetChatSettingsHandler(gson),
        "update_settings" to UpdateChatSettingsHandler(gson),
    )

    fun handleRawMessage(rawRequest: String, project: Project): String {
        return gson.toJson(handleMessage(rawRequest, project))
    }

    private fun handleMessage(rawRequest: String, project: Project): ChatMessageResponse {
        val request = gson.fromJson(rawRequest, ChatMessageRequest::class.java)
        try {
            val commandHandler = commandHandlers[request.command] ?: return noHandlerError(request)

            val responsePayload = commandHandler.handleRaw(request.data, project) ?: return ChatMessageResponse(request.id)

            return ChatMessageResponse(request.id, responsePayload)
        } catch (e: Exception) {
            return ChatMessageResponse(request.id, error = e.message ?: e.toString())
        }
    }

    private fun noHandlerError(request: ChatMessageRequest): ChatMessageResponse {
        return ChatMessageResponse(request.id, error = "No handler for ${request.command}")
    }
}
