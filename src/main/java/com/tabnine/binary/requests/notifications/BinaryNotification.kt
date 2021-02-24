package com.tabnine.binary.requests.notifications

import com.google.gson.annotations.SerializedName

data class BinaryNotification(
    var id: String? = null,
    var message: String? = null,
    var options: List<NotificationOptions>? = null,
    @SerializedName("notification_type")
    var notificationType: String?,
    val state: Object?,
)
