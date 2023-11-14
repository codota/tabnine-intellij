package com.tabnineCommon.binary.requests.capabilities

import com.google.gson.JsonElement
import com.tabnineCommon.binary.BinaryRequest

class CapabilitiesAsJsonRequest : BinaryRequest<JsonElement> {
    override fun response(): Class<JsonElement> {
        return JsonElement::class.java
    }

    override fun serialize(): Any {
        return mapOf("Features" to emptyMap<Any, Any>())
    }
}
