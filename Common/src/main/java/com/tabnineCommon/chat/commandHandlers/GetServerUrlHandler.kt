package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.config.ChatCommunicationKind
import com.tabnineCommon.binary.requests.config.ChatCommunicatorAddressRequest
import com.tabnineCommon.general.DependencyContainer

data class ServerUrlRequest(val kind: ChatCommunicationKind)
data class ServerUrlResponse(val serverUrl: String)

class GetServerUrlHandler(gson: Gson) : ChatMessageHandler<ServerUrlRequest, ServerUrlResponse>(gson) {
    override fun handle(payload: ServerUrlRequest?, project: Project): ServerUrlResponse {
        if (payload == null) {
            throw IllegalArgumentException("ServerUrlRequest is null")
        }

        val request = ChatCommunicatorAddressRequest(payload.kind)
        var response = DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(
            request
        )

        // retry if failed
        // might happen if binary just restarted
        if (response == null) {
            response = DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(
                request
            )
        }

        return ServerUrlResponse(response.address)
    }

    override fun deserializeRequest(data: JsonElement?): ServerUrlRequest {
        return gson.fromJson(data, ServerUrlRequest::class.java)
    }
}
