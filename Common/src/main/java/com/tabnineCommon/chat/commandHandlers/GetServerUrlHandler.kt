package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.config.ChatCommunicationKind
import com.tabnineCommon.binary.requests.config.ChatCommunicatorAddressRequest
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.commandHandlers.utils.getServerUrl
import com.tabnineCommon.general.DependencyContainer

data class ServerUrlRequest(val kind: ChatCommunicationKind)
data class ServerUrlResponse(val serverUrl: String)

class GetServerUrlHandler(gson: Gson) : ChatMessageHandler<ServerUrlRequest, ServerUrlResponse>(gson) {
    override fun handle(payload: ServerUrlRequest?, project: Project): ServerUrlResponse {
        if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.CHAT_URL_FROM_BINARY) && payload != null) {
            val response = DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(ChatCommunicatorAddressRequest(payload.kind))
            return ServerUrlResponse(response.address)
        }

        val serverUrl = getServerUrl() ?: "https://api.tabnine.com"
        return ServerUrlResponse(serverUrl)
    }

    override fun deserializeRequest(data: JsonElement?): ServerUrlRequest {
        return gson.fromJson(data, ServerUrlRequest::class.java)
    }
}
