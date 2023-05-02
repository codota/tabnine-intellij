package com.tabnineCommon.binary.requests.config

import com.tabnineCommon.binary.BinaryRequest

class StateRequest : BinaryRequest<StateResponse> {
    override fun response(): Class<StateResponse> {
        return StateResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("State" to Any())
    }

    override fun validate(response: StateResponse): Boolean {
        return true
    }
}
