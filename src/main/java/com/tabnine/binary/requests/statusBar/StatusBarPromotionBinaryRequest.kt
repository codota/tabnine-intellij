package com.tabnine.binary.requests.statusBar

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.exceptions.TabNineInvalidResponseException

class StatusBarPromotionBinaryRequest: BinaryRequest<StatusBarPromotionBinaryResponse> {
    override fun response(): Class<StatusBarPromotionBinaryResponse> {
        return StatusBarPromotionBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("StatusBar" to emptyMap<Any, Any>())
    }

    override fun shouldBeAllowed(e: TabNineInvalidResponseException): Boolean {
        // If there is no promotion to show, the binary returns "null" as response, so we just ignore it.
        return e.rawResponse.filter { it == "null" }.isPresent
    }
}
