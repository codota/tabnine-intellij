package com.tabnineCommon.binary.requests.notifications

import com.tabnineCommon.binary.BinaryRequest

class NotificationsBinaryRequest : BinaryRequest<NotificationsBinaryResponse> {
    override fun response(): Class<NotificationsBinaryResponse> {
        return NotificationsBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Notifications" to emptyMap<Any, Any>())
    }
}
