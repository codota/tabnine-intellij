package com.tabnine.binary.requests.statusBar

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryResponse

data class StatusBarPromotionBinaryResponse(
    val id: String?,
    val message: String?,
    val actions: List<String>?,
    @SerializedName("notification_type") val notificationType: String?,
    val state: Object?,
    @SerializedName("duration_seconds") val durationSeconds: Long?,
) : BinaryResponse
