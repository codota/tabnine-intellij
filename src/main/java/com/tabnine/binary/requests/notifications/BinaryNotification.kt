package com.tabnine.binary.requests.notifications

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue

data class BinaryNotification(
        var id: String? = null,
        var message: String? = null,
        var options: List<NotificationOptions>? = null,
        @JsonRawValue @JsonProperty(value = "notification_type")
        var notificationType: String?
)
