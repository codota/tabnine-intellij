package com.tabnineCommon.binary.requests.capabilities

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.EmptyResponse

class RefreshRemotePropertiesRequest : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("RefreshRemoteProperties" to emptyMap<Any, Any>())
    }
}
