package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.tabnineCommon.lifecycle.BinaryStateService

data class GetUserResponsePayload(val token: String, val username: String)

class GetUserHandler(gson: Gson) : ChatMessageHandler<EmptyPayload, GetUserResponsePayload>(gson) {
    override fun handle(payload: EmptyPayload?, project: Project): GetUserResponsePayload? {
        val stateResponse = ServiceManager.getService(BinaryStateService::class.java).lastStateResponse

        val token = stateResponse.accessToken ?: return null
        val username = stateResponse.userName ?: return null

        return GetUserResponsePayload(token, username)
    }

    override fun deserialize(data: JsonElement?): EmptyPayload? {
        return null
    }
}
