package com.tabnineCommon.chat.commandHandlers.chatSettings

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler

class GetChatSettingsHandler(gson: Gson) : ChatMessageHandler<Unit, ChatSettingsProps>(gson) {
    private val chatSettings = ChatSettings(gson)

    override fun handle(payload: Unit?, project: Project): ChatSettingsProps {
        return chatSettings.get()
    }

    override fun deserializeRequest(data: JsonElement?) {}
}
