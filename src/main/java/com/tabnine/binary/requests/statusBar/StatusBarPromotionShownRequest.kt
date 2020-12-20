package com.tabnine.binary.requests.statusBar

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.selection.SetStateBinaryResponse
import com.tabnine.general.StaticConfig

class StatusBarPromotionShownRequest(private val text: String) : BinaryRequest<SetStateBinaryResponse> {
    override fun response(): Class<SetStateBinaryResponse> {
        return SetStateBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SetState" to mapOf("state_type" to mapOf("StatusShown" to this)))
    }

    override fun validate(response: SetStateBinaryResponse): Boolean {
        return StaticConfig.SET_STATE_RESPONSE_RESULT_STRING == response.result
    }
}
