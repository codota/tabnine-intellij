package com.tabnineCommon.binary.requests.notifications.shown

import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.exceptions.TabNineInvalidResponseException
import com.tabnineCommon.binary.requests.EmptyResponse
import com.tabnineCommon.binary.requests.autocomplete.CompletionMetadata

enum class SuggestionDroppedReason {
    ManualCancel,
    ScrollLookAhead,
    TextDeletion,
    UserNotTypedAsSuggested,
    CaretMoved,
    FocusChanged,
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
