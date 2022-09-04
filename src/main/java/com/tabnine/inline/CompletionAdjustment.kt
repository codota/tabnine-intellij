package com.tabnine.inline

import com.tabnine.binary.requests.autocomplete.AutocompleteRequest
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse

interface CompletionAdjustment {
    fun adjustRequest(autocompleteRequest: AutocompleteRequest): AutocompleteRequest
    fun adjustResponse(autocompleteResponse: AutocompleteResponse): AutocompleteResponse
    val type: CompletionAdjustmentType
}
