package com.tabnine.binary.requests.notifications.actions

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.EmptyResponse

data class HoverActionRequest(
    val id: String,
    val selected: String,
    val state: Any?,
    val message: String?,
    @SerializedName("notification_type")
    val notificationType: String?,
    val actions: Array<Any>,
) : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("HoverAction" to this)
    }
}
