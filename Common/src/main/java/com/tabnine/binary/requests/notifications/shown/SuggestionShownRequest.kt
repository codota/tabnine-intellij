package com.tabnine.binary.requests.notifications.shown

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.exceptions.TabNineInvalidResponseException
import com.tabnine.binary.requests.EmptyResponse
import com.tabnine.binary.requests.autocomplete.CompletionMetadata

data class SuggestionShownRequest(
    var net_length: Int,
    var filename: String,
    var metadata: CompletionMetadata
) : BinaryRequest<EmptyResponse> {
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
