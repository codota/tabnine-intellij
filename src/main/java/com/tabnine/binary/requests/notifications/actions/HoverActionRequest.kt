package com.tabnine.binary.requests.notifications.actions

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse
import com.tabnine.general.NotificationOption

data class HoverActionRequest(
        val id: String,
        val selected: String,
        val state: Object?,
        val message: String?,
        @SerializedName("notification_type")
        val notificationType: String?,
        val actions: Array<String>,
        ): BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("HoverAction" to this)
    }
}