package com.tabnineCommon.inline

enum class CompletionOrder {
    PREVIOUS {
        override fun diff() = -1
    },
    NEXT {
        override fun diff() = 1
    };

    abstract fun diff(): Int
}
