package com.tabnineCommon.chat

data class ChatState private constructor(
    val enabled: Boolean,
    val loading: Boolean,
    val chatDisabledReason: ChatDisabledReason?
) {
    companion object {
        @JvmStatic
        fun enabled() = ChatState(enabled = true, loading = false, null)

        @JvmStatic
        fun loading() = ChatState(enabled = false, loading = true, null)

        @JvmStatic
        fun disabled(reason: ChatDisabledReason) = ChatState(
            enabled = false,
            loading =
            false,
            reason
        )
    }
}

enum class ChatDisabledReason {
    AUTHENTICATION_REQUIRED,
    PREVIEW_ENDED,
    FEATURE_REQUIRED,
    PART_OF_A_TEAM_REQUIRED,
}
