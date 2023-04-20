package com.tabnineCommon.binary.requests.notifications.shown

import com.google.gson.annotations.SerializedName
import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.selection.SetStateBinaryResponse
import com.tabnineCommon.general.StaticConfig

data class HoverShownRequest(
    var id: String,
    var text: String?,
    @SerializedName("notification_type")
    val notificationType: String?,
    val state: Any?,
) : BinaryRequest<SetStateBinaryResponse> {
    override fun response(): Class<SetStateBinaryResponse> {
        return SetStateBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SetState" to mapOf("state_type" to mapOf("HoverShown" to this)))
    }

    override fun validate(response: SetStateBinaryResponse): Boolean {
        return StaticConfig.SET_STATE_RESPONSE_RESULT_STRING == response.result
    }
}
