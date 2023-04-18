package com.tabnine.inline

import com.tabnine.binary.requests.autocomplete.AutocompleteRequest
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse
import com.tabnine.general.SuggestionTrigger

interface CompletionAdjustment {
    fun adjustRequest(autocompleteRequest: AutocompleteRequest): AutocompleteRequest = autocompleteRequest
    fun adjustResponse(autocompleteResponse: AutocompleteResponse): AutocompleteResponse = autocompleteResponse
    val suggestionTrigger: SuggestionTrigger
}
