package com.tabnineCommon.binary.requests.config

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryRequest

enum class ChatCommunicationKind {
    @SerializedName("forward")
    Forward,
    @SerializedName("root")
    Root,
}

class ChatCommunicatorAddressRequest(val kind: ChatCommunicationKind) : BinaryRequest<ChatCommunicatorAddressResponse> {
    override fun response(): Class<ChatCommunicatorAddressResponse> {
        return ChatCommunicatorAddressResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("ChatCommunicatorAddress" to this)
    }

    override fun validate(response: ChatCommunicatorAddressResponse): Boolean {
        return true
    }
}
