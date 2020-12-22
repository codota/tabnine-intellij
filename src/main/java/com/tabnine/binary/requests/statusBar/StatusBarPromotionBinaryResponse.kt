package com.tabnine.binary.requests.statusBar

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryResponse
import com.tabnine.binary.requests.notifications.NotificationActions

data class StatusBarPromotionBinaryResponse(
    var id: String?,
    var message: String?,
    var action: NotificationActions?,
    @SerializedName("notification_type") var notificationType: String?
) : BinaryResponse
