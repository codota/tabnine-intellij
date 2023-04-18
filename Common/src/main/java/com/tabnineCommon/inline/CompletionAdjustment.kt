package com.tabnineCommon.inline

import com.tabnineCommon.binary.requests.autocomplete.AutocompleteRequest
import com.tabnineCommon.binary.requests.autocomplete.AutocompleteResponse
import com.tabnineCommon.general.SuggestionTrigger

interface CompletionAdjustment {
    fun adjustRequest(autocompleteRequest: AutocompleteRequest): AutocompleteRequest = autocompleteRequest
    fun adjustResponse(autocompleteResponse: AutocompleteResponse): AutocompleteResponse = autocompleteResponse
    val suggestionTrigger: SuggestionTrigger
}
