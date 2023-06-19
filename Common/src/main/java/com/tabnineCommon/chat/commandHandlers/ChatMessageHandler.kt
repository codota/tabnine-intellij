package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project

abstract class ChatMessageHandler<RequestPayload, ResponsePayload>(protected val gson: Gson) {
    fun handleRaw(data: JsonElement?, project: Project): ResponsePayload? {
        val payload = deserialize(data)
        return handle(payload, project)
    }

    abstract fun handle(payload: RequestPayload?, project: Project): ResponsePayload?

    abstract fun deserialize(data: JsonElement?): RequestPayload?
}

class EmptyPayload
