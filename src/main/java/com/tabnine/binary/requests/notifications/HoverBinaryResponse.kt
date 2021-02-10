package com.tabnine.binary.requests.notifications

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryResponse
import com.tabnine.general.NotificationOption

data class HoverBinaryResponse(
        val id: String,
        val message: String,
        val title: String?,
        val options: Array<NotificationOption>,
        @SerializedName("notification_type")
        val notificationType: String?,
        val state: String?,
) : BinaryResponse {

}