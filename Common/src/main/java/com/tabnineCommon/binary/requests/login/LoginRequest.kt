package com.tabnineCommon.binary.requests.login

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.EmptyResponse

class LoginRequest : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Login" to this)
    }
}
