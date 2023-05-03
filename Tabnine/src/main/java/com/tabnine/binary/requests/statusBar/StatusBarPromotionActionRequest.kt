package com.tabnine.binary.requests.statusBar

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.EmptyResponse

data class StatusBarPromotionActionRequest(var id: String?, var selected: String?, var actions: List<Any>?) :
    BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("StatusBarAction" to this)
    }
}
