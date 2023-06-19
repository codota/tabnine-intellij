package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson

abstract class ChatMessageHandler<RequestPayload, ResponsePayload>(protected val gson: Gson) {
    fun handleRaw(data: Any?): ResponsePayload? {
        val payload = deserialize(data)
        return handle(payload)
    }

    abstract fun handle(payload: RequestPayload?): ResponsePayload?

    abstract fun deserialize(data: Any?): RequestPayload?
}

class EmptyPayload
