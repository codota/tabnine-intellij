package com.tabnine.binary.requests.notifications.shown

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.notifications.HoverBinaryResponse
import com.tabnine.binary.requests.selection.SetStateBinaryResponse
import com.tabnine.general.StaticConfig

data class HintShownRequest(
    var id: String,
    var text: String?,
    @SerializedName("notification_type")
    val notificationType: String?,
    val state: Any?,
) : BinaryRequest<SetStateBinaryResponse> {

    constructor(hover: HoverBinaryResponse) : this(hover.id, hover.title, hover.notificationType, null)

    override fun response(): Class<SetStateBinaryResponse> {
        return SetStateBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SetState" to mapOf("state_type" to mapOf("HintShown" to this)))
    }

    override fun validate(response: SetStateBinaryResponse): Boolean {
        return StaticConfig.SET_STATE_RESPONSE_RESULT_STRING == response.result
    }
}
