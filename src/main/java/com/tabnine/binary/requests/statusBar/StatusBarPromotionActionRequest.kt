package com.tabnine.binary.requests.statusBar

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse

class StatusBarPromotionActionRequest(private val id: String?, private val selected: String?): BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("StatusBarAction" to this)
    }
}