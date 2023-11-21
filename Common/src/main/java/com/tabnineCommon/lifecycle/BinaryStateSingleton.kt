package com.tabnineCommon.lifecycle

import com.intellij.util.messages.Topic
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.general.TopicBasedState
import java.util.function.Consumer

class BinaryStateSingleton private constructor() :
    TopicBasedState<StateResponse, BinaryStateSingleton.OnChange>(
        TOPIC
    ) {
    companion object {
        private val TOPIC = Topic.create("Binary State Changed Notifier", OnChange::class.java)

        @JvmStatic
        val instance = BinaryStateSingleton()
    }

    public fun interface OnChange : Consumer<StateResponse>
}
