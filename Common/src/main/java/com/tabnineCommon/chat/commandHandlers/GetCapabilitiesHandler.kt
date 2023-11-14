package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.capabilities.CapabilitiesAsJsonRequest
import com.tabnineCommon.general.DependencyContainer

data class GetCapabilitiesResponsePayload(val enabledFeatures: Array<String>)

class GetCapabilitiesHandler(gson: Gson) : ChatMessageHandler<Unit, GetCapabilitiesResponsePayload>(gson) {
    val binaryRequestFacade: BinaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun handle(payload: Unit?, project: Project): GetCapabilitiesResponsePayload {
        val response = binaryRequestFacade.executeRequest(CapabilitiesAsJsonRequest())
        return GetCapabilitiesResponsePayload(response.asJsonObject.getAsJsonArray("enabled_features").map { it.asString }.toTypedArray())
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
