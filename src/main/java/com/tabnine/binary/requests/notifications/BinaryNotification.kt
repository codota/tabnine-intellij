package com.tabnine.binary.requests.notifications

data class BinaryNotification(
        var id: String? = null,
        var message: String? = null,
        var options: List<NotificationOptions>? = null,
)
