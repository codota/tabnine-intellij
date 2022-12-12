package com.tabnine.binary.requests.notifications.shown

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.requests.EmptyResponse
import com.tabnine.general.CompletionKind
import com.tabnine.general.CompletionOrigin

data class SuggestionShownRequest(var origin: CompletionOrigin?, var completion_kind: CompletionKind?, var net_length: Int) : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SuggestionShown" to this)
    }
}
