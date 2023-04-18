package com.tabnineCommon.binary.requests.notifications.shown

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.requests.selection.SetStateBinaryResponse
import com.tabnineCommon.general.StaticConfig

data class SnippetShownRequest(var filename: String, var snippet_context: Map<String, Any>) :
    BinaryRequest<SetStateBinaryResponse> {
    override fun response(): Class<SetStateBinaryResponse> {
        return SetStateBinaryResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SetState" to mapOf("state_type" to mapOf("SnippetShown" to this)))
    }

    override fun validate(response: SetStateBinaryResponse): Boolean {
        return StaticConfig.SET_STATE_RESPONSE_RESULT_STRING == response.result
    }
}
