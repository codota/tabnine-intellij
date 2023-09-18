package com.tabnineCommon.binary.requests.fileLifecycle

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.EmptyResponse

data class PrefetchRequest(private val filename: String) :
    BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Prefetch" to this)
    }
}
