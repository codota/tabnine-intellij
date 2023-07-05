package com.tabnineCommon.chat.commandHandlers.chatSettings

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler

class UpdateChatSettingsHandler(gson: Gson) : ChatMessageHandler<ChatSettingsProps, Unit>(gson) {
    private val chatSettings = ChatSettings(gson)

    override fun handle(payload: ChatSettingsProps?, project: Project) {
        payload?.let { chatSettings.save(it) }
    }

    override fun deserializeRequest(data: JsonElement?): ChatSettingsProps? {
        return gson.fromJson(data, ChatSettingsProps::class.java)
    }
}
