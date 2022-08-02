package com.tabnine.binary.requests.autocomplete

data class SnippetContext(
    val stop_reason: String?,
    val generated_tokens: Int?,
    val user_intent: UserIntent,
    val intent_metadata: SnippetIntentMetadata?
)

data class SnippetIntentMetadata(
    val current_line_indentation: Int?,
    val previous_line_indentation: Int?,
    val triggered_after_character: Char?
)
