package com.tabnine.binary.requests.notifications.shown

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.exceptions.TabNineInvalidResponseException
import com.tabnine.binary.requests.EmptyResponse
import com.tabnine.general.CompletionKind
import com.tabnine.general.CompletionOrigin

data class SuggestionShownRequest(var origin: CompletionOrigin?, var completion_kind: CompletionKind?, var net_length: Int, var language: String) : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SuggestionShown" to this)
    }

    override fun shouldBeAllowed(e: TabNineInvalidResponseException): Boolean {
        return true
    }
}
