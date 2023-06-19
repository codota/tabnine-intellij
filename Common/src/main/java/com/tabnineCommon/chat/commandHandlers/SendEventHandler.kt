package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRequestFacade

data class SendEventRequestPayload(val eventName: String, val properties: Map<String, String>? = null)

class SendEventHandler(gson: Gson) : ChatMessageHandler<SendEventRequestPayload, EmptyPayload>(gson) {
    private val binaryRequestFacade = instanceOfBinaryRequestFacade()

    override fun handle(payload: SendEventRequestPayload?, project: Project): EmptyPayload? {
        if (payload == null) return null

        binaryRequestFacade.executeRequest(EventRequest(payload.eventName, payload.properties ?: emptyMap()))

        return null
    }

    override fun deserialize(data: JsonElement?): SendEventRequestPayload? {
        return gson.fromJson(data, SendEventRequestPayload::class.java)
    }
}
