package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.tabnineCommon.general.ServiceLevel
import com.tabnineCommon.lifecycle.BinaryStateService

data class GetUserResponsePayload(val token: String, val username: String, val serviceLevel: ServiceLevel, val avatarUrl: String? = null)

class GetUserHandler(gson: Gson) : ChatMessageHandler<Unit, GetUserResponsePayload>(gson) {
    override fun handle(payload: Unit?, project: Project): GetUserResponsePayload? {
        val stateResponse = ServiceManager.getService(BinaryStateService::class.java).lastStateResponse ?: return null

        val token = stateResponse.accessToken ?: return null
        val username = stateResponse.userName ?: return null
        val serviceLevel = stateResponse.serviceLevel ?: return null
        val avatarUrl = stateResponse.avatarUrl

        return GetUserResponsePayload(token, username, serviceLevel, avatarUrl)
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
