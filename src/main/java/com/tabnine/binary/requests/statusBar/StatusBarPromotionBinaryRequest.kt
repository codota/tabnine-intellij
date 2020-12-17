package com.tabnine.binary.requests.statusBar

import com.tabnine.binary.BinaryRequest

class StatusBarPromotionBinaryRequest: BinaryRequest<StatusBarPromotionBinaryResponse> {
    override fun response(): Class<StatusBarPromotionBinaryResponse> {
        return StatusBarPromotionBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("StatusBar" to emptyMap<Any, Any>())
    }
}
