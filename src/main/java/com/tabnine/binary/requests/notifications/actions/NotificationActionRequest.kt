package com.tabnine.binary.requests.notifications.actions

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse

data class NotificationActionRequest(
    var id: String?,
    var selected: String?,
    var message: String?,
    @JsonRawValue @JsonProperty(value = "notification_type") var notificationType: String?
) : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("NotificationAction" to this)
    }
}