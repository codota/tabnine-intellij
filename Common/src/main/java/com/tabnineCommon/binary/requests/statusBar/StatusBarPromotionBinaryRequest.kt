package com.tabnineCommon.binary.requests.statusBar

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.exceptions.TabNineInvalidResponseException

class StatusBarPromotionBinaryRequest :
    BinaryRequest<StatusBarPromotionBinaryResponse> {
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
