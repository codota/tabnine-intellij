package com.tabnine.binary.requests.autocomplete

data class SnippetContext(
    val stop_reason: String?,
    val generated_tokens: Int?,
    val user_intent: UserIntent
)
