package com.tabnineCommon.binary.requests.notifications.shown

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.exceptions.TabNineInvalidResponseException
import com.tabnineCommon.binary.requests.EmptyResponse
import com.tabnineCommon.binary.requests.autocomplete.CompletionMetadata

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
