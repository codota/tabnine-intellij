package com.tabnineCommon.chat

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.GetEditorContextHandler
import com.tabnineCommon.chat.commandHandlers.GetUserHandler
import com.tabnineCommon.chat.commandHandlers.SendEventHandler

data class ChatMessageRequest(val id: String, val command: String, val data: JsonElement? = null)
data class ChatMessageResponse(val id: String, val payload: Any? = null)

class ChatMessagesRouter {
    private val gson = GsonBuilder().create()
    private val commandHandlers = mapOf<String, ChatMessageHandler<*, *>>(
        "get_user" to GetUserHandler(gson),
        "send_event" to SendEventHandler(gson),
        "get_editor_context" to GetEditorContextHandler(gson),
    )

    fun handleMessage(rawRequest: String, project: Project): String? {
        val request = gson.fromJson(rawRequest, ChatMessageRequest::class.java)

        val commandHandler = commandHandlers[request.command] ?: return null

        val responsePayload = commandHandler.handleRaw(request.data, project) ?: return null

        return gson.toJson(ChatMessageResponse(request.id, responsePayload))
    }
}
