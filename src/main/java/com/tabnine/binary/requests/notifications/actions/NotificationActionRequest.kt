package com.tabnine.binary.requests.notifications.actions

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse

data class NotificationActionRequest(var id: String?, var selected: String?, var message: String?, var notification_type: NotificationReasonType? = null)
    : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("NotificationAction" to this)
    }
}