package com.tabnineCommon.lifecycle

import com.intellij.util.messages.Topic
import com.tabnineCommon.capabilities.Capabilities
import com.tabnineCommon.general.TopicBasedState
import java.util.function.Consumer

class CapabilitiesStateSingleton private constructor() :
    TopicBasedState<Capabilities, CapabilitiesStateSingleton.OnChange>(
        TOPIC
    ) {
    companion object {
        private val TOPIC = Topic.create("com.tabnine.capabilties", OnChange::class.java)

        @JvmStatic
        val instance = CapabilitiesStateSingleton()
    }

    public fun interface OnChange : Consumer<Capabilities>
}
