package com.tabnine.binary.requests.analytics

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse

data class EventRequest(var name: String, var properties: Map<String, String>?) :
    BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Event" to this)
    }
}
