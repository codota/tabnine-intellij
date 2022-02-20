package com.tabnine.binary.requests.userSnippets

import com.google.gson.annotations.SerializedName
import com.tabnine.binary.BinaryRequest

data class SaveSnippetRequest(
    val code: String,
    val filename: String,
    @SerializedName("start_offset")
    val startOffset: Int,
    @SerializedName("end_offset")
    val endOffset: Int,
) : BinaryRequest<SaveSnippetResponse> {
    override fun response(): Class<SaveSnippetResponse> {
        return SaveSnippetResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("SaveSnippet" to this)
    }
}
