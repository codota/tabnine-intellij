package com.tabnineCommon.inline

import com.tabnineCommon.binary.requests.autocomplete.AutocompleteRequest
import com.tabnineCommon.binary.requests.autocomplete.AutocompleteResponse
import com.tabnineCommon.binary.requests.autocomplete.ResultEntry
import com.tabnineCommon.general.SuggestionTrigger
import java.util.Arrays

class LookAheadCompletionAdjustment(private val userPrefix: String, private val focusedCompletion: String) : CompletionAdjustment {
    override fun adjustRequest(autocompleteRequest: AutocompleteRequest): AutocompleteRequest {
        autocompleteRequest.before = (
            autocompleteRequest.before.substring(
                0, autocompleteRequest.before.length - userPrefix.length
            ) +
                focusedCompletion
            )
        return autocompleteRequest
    }

    override fun adjustResponse(autocompleteResponse: AutocompleteResponse): AutocompleteResponse {
        autocompleteResponse.old_prefix = userPrefix
        autocompleteResponse.results = Arrays.stream(autocompleteResponse.results)
            .filter { resultEntry -> resultEntry.new_prefix.startsWith(focusedCompletion) }
            .toArray { size -> arrayOfNulls<ResultEntry>(size) }
        return autocompleteResponse
    }

    override val suggestionTrigger: SuggestionTrigger
        get() = SuggestionTrigger.LookAhead
}
