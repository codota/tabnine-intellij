package com.tabnine.binary.requests.config

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.exceptions.TabNineInvalidResponseException
import java.util.*

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