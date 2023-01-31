package com.tabnine.binary.requests.notifications.shown

import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.exceptions.TabNineInvalidResponseException
import com.tabnine.binary.requests.EmptyResponse
import com.tabnine.binary.requests.autocomplete.CompletionMetadata

enum class SuggestionDroppedReason {
    ManualCancel,
    ScrollLookAhead,
    TextDeletion,
    UserNotTypedAsSuggested,
}

data class SuggestionDroppedRequest(
    var net_length: Int,
    var reason: SuggestionDroppedReason? = null,
    var filename: String? = null,
    var metadata: CompletionMetadata? = null
) : BinaryRequest<EmptyResponse> {
    override fun response(): Class<EmptyResponse> {
        return EmptyResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SuggestionDropped" to this)
    }

    override fun shouldBeAllowed(e: TabNineInvalidResponseException): Boolean {
        return true
    }
}
