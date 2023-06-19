package com.tabnineCommon.chat.commandHandlers

import com.google.gson.Gson

data class GetUserResponsePayload(val token: String, val username: String)

class GetUserHandler(gson: Gson) : ChatMessageHandler<EmptyPayload, GetUserResponsePayload>(gson) {
    override fun handle(payload: EmptyPayload?): GetUserResponsePayload {
        return GetUserResponsePayload("kaki", "pipi")
    }

    override fun deserialize(data: Any?): EmptyPayload? {
        return null
    }
}
