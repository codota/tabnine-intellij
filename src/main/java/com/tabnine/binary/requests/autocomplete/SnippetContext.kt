package com.tabnine.binary.requests.autocomplete

data class SnippetContext(
    val snippet_id: String?,
    val user_intent: UserIntent,
    val intent_metadata: SnippetIntentMetadata?,
    val additional_properties: Map<String, Any>,
)

data class SnippetIntentMetadata(
    val current_line_indentation: Int?,
    val previous_line_indentation: Int?,
    val triggered_after_character: Char?
)
