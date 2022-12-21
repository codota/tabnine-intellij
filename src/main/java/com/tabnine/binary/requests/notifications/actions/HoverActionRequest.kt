package com.tabnine.binary.requests.notifications.actions

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.exceptions.TabNineInvalidResponseException
import com.tabnine.binary.requests.EmptyResponse

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

    override fun shouldBeAllowed(e: TabNineInvalidResponseException): Boolean {
        return true
    }
}
