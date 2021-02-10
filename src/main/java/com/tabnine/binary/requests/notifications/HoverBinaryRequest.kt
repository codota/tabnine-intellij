package com.tabnine.binary.requests.notifications

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.exceptions.TabNineInvalidResponseException

class HoverBinaryRequest : BinaryRequest<HoverBinaryResponse> {
    override fun response(): Class<HoverBinaryResponse> {
        return HoverBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Hover" to Any())
    }

}