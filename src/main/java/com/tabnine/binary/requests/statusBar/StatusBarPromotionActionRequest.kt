package com.tabnine.binary.requests.statusBar

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse
import com.tabnine.binary.requests.notifications.NotificationActions

data class StatusBarPromotionActionRequest(var id: String?, var selected: String?, var action: NotificationActions?) :
    BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("StatusBarAction" to this)
    }
}