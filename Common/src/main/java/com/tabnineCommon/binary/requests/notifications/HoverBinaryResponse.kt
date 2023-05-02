package com.tabnineCommon.binary.requests.notifications

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryResponse
import com.tabnineCommon.general.NotificationOption

data class HoverBinaryResponse(
    val id: String,
    val message: String,
    val title: String?,
    val options: Array<NotificationOption>,
    @SerializedName("notification_type")
    val notificationType: String?,
    val state: Any?,
) : BinaryResponse
