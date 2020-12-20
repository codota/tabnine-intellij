package com.tabnine.binary.requests.statusBar

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse
import com.tabnine.binary.requests.notifications.NotificationActions

class StatusBarPromotionActionRequest(private val id: String?, private val selected: String?, private val action: NotificationActions?): BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("StatusBarAction" to this)
    }
}