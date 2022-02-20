package com.tabnine.binary.requests.userSnippets

import com.tabnine.binary.BinaryResponse

data class SaveSnippetErrorResponse(val error: String)

private const val SUCCESS = "Success"

data class SaveSnippetResponse(val result: Any?) : BinaryResponse {
    fun asError(): SaveSnippetErrorResponse? {
        if (result is Map<*, *>) {
            return result["Error"]?.let {
                if (it is String) {
                    return SaveSnippetErrorResponse(it)
                }
                return null
            }
        }
        return null
    }

    fun isSuccess() = result is String && result == SUCCESS
}
