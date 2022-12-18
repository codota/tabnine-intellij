package com.tabnine.binary.requests.autocomplete

data class SnippetContext(
    val stop_reason: String?,
    val generated_tokens: Int?,
    val user_intent: UserIntent,
    val intent_metadata: SnippetIntentMetadata?,
    val response_time_ms: Int?,
    val is_cached: Boolean?,
    val context_len: Int?,
    val first_token_score: String?,
    val prompt_additional_properties: PromptAdditionalProperties?,
    val snippet_id: String?,
)

data class PromptAdditionalProperties(
    val resolved_dependencies: Boolean?,
    val max_prompt_length: Int?,
    val prompt_enrichment_lengths: PromptEnrichmentLengths?,
)

data class PromptEnrichmentLengths(
    val prompt_until_imports_len: Int?,
    val prompt_after_imports_len: Int?,
    val dependencies_len: Int?
)

data class SnippetIntentMetadata(
    val current_line_indentation: Int?,
    val previous_line_indentation: Int?,
    val triggered_after_character: Char?
)
