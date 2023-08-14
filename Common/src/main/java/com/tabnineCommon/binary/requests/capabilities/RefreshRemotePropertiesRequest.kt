package com.tabnineCommon.binary.requests.capabilities

import com.tabnineCommon.binary.BinaryRequest

class RefreshRemotePropertiesRequest : BinaryRequest<RefreshRemotePropertiesResponse> {
    override fun response(): Class<RefreshRemotePropertiesResponse> {
        return RefreshRemotePropertiesResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("RefreshRemoteProperties" to emptyMap<Any, Any>())
    }
}
