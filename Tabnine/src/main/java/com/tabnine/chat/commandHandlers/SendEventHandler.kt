package com.tabnine.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRequestFacade

data class SendEventRequestPayload(val eventName: String, val properties: Map<String, String>? = null)

class SendEventHandler(gson: Gson) : ChatMessageHandler<SendEventRequestPayload, Unit>(gson) {
    private val binaryRequestFacade = instanceOfBinaryRequestFacade()

    override fun handle(payload: SendEventRequestPayload?, project: Project) {
        if (payload == null) return

        binaryRequestFacade.executeRequest(EventRequest(payload.eventName, payload.properties ?: emptyMap()))
    }

    override fun deserializeRequest(data: JsonElement?): SendEventRequestPayload? {
        return gson.fromJson(data, SendEventRequestPayload::class.java)
    }
}
