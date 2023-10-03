package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.utils.getServerUrl

data class ServerUrlResponse(val serverUrl: String)

class GetServerUrlHandler(gson: Gson) : ChatMessageHandler<Unit, ServerUrlResponse>(gson) {
    override fun handle(payload: Unit?, project: Project): ServerUrlResponse {
        val serverUrl = getServerUrl() ?: "https://api.tabnine.com"
        return ServerUrlResponse(serverUrl)
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
