package com.tabnine.binary.requests.statusBar

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import com.tabnine.binary.BinaryResponse
import com.tabnine.binary.requests.notifications.NotificationActions

@JsonIgnoreProperties(ignoreUnknown = true)
data class StatusBarPromotionBinaryResponse(
        var id: String?,
        var message: String?,
        var action: NotificationActions?,
        @JsonRawValue @JsonProperty(value = "notification_type") var notificationType: String?
) : BinaryResponse
