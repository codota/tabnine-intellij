package com.tabnine.binary.requests.statusBar

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryResponse

data class StatusBarPromotionBinaryResponse(
    var id: String?,
    var message: String?,
    var actions: List<String>?,
    @SerializedName("notification_type") var notificationType: String?,
    val state: Object?,
) : BinaryResponse
