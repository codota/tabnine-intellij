package com.tabnine.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.tabnineCommon.lifecycle.BinaryStateService

data class GetUserResponsePayload(val token: String, val username: String, val avatarURL: String)

class GetUserHandler(gson: Gson) : ChatMessageHandler<Unit, GetUserResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): GetUserResponsePayload? {
        val stateResponse = ServiceManager.getService(BinaryStateService::class.java).lastStateResponse

        val token = stateResponse.accessToken ?: return null
        val username = stateResponse.userName ?: return null
        val avatarURL = stateResponse.avatarURL ?: return null

        return GetUserResponsePayload(token, username, avatarURL)
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
