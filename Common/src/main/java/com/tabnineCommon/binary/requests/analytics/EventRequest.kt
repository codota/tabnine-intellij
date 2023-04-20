package com.tabnineCommon.binary.requests.analytics

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.EmptyResponse

data class EventRequest(var name: String, var properties: Map<String, String>?) :
    BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Event" to this)
    }
}
