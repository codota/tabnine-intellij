package com.tabnineCommon.chat

import com.google.gson.GsonBuilder
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.GetUserHandler

data class ChatMessageRequest(val id: String, val command: String, val data: Any? = null)
data class ChatMessageResponse(val id: String, val payload: Any? = null)

class ChatMessagesRouter {
    private val gson = GsonBuilder().create()
    private val commandHandlers = mapOf<String, ChatMessageHandler<*, *>>(
        "get_user" to GetUserHandler(gson)
    )

    fun handleMessage(rawRequest: String): String? {
        val request = gson.fromJson(rawRequest, ChatMessageRequest::class.java)

        val commandHandler = commandHandlers[request.command] ?: return null

        val responsePayload = commandHandler.handleRaw(request.data) ?: return null

        return gson.toJson(ChatMessageResponse(request.id, responsePayload))
    }
}
