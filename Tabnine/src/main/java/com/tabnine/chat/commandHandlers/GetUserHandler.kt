package com.tabnine.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.tabnineCommon.lifecycle.BinaryStateService

data class GetUserResponsePayload(val token: String, val username: String, val avatarUrl: String? = null)

class GetUserHandler(gson: Gson) : ChatMessageHandler<Unit, GetUserResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): GetUserResponsePayload? {
        val stateResponse = ServiceManager.getService(BinaryStateService::class.java).lastStateResponse

        val token = stateResponse.accessToken ?: return null
        val username = stateResponse.userName ?: return null
        val avatarUrl = stateResponse.avatarUrl

        return GetUserResponsePayload(token, username, avatarUrl)
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
