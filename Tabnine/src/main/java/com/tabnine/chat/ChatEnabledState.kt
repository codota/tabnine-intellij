package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.util.messages.Topic
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.capabilities.Capabilities
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.ChatDisabledReason
import com.tabnineCommon.chat.ChatFrame
import com.tabnineCommon.chat.ChatState
import com.tabnineCommon.general.TopicBasedNonNullState
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import com.tabnineCommon.lifecycle.CapabilitiesStateSingleton
import java.util.function.Consumer

class ChatEnabledState private constructor() : ChatFrame.UseChatEnabledState,
    TopicBasedNonNullState<ChatState, ChatEnabledChanged>(
        ENABLED_TOPIC, ChatState.loading()
    ) {
    companion object {
        private val ENABLED_TOPIC: Topic<ChatEnabledChanged> =
            Topic.create("ChatEnabled", ChatEnabledChanged::class.java)

        val instance = ChatEnabledState()
    }

    init {
        updateEnabled(
            BinaryStateSingleton.instance.get(),
            CapabilitiesStateSingleton.instance.get()
        )

        BinaryStateSingleton.instance.onChange {
            updateEnabled(it, CapabilitiesStateSingleton.instance.get())
        }

        CapabilitiesStateSingleton.instance.onChange {
            updateEnabled(BinaryStateSingleton.instance.get(), it)
        }
    }

    private fun updateEnabled(
        binaryState: StateResponse?,
        capabilities: Capabilities?
    ) {
        if (capabilities == null || binaryState == null || !capabilities.isReady()) {
            return
        }

        val isLoggedIn = binaryState.isLoggedIn ?: return

        val hasCapability =
            capabilities.anyEnabled(Capability.ALPHA, Capability.TABNINE_CHAT, Capability.PREVIEW_CAPABILITY)

        if (isLoggedIn && hasCapability) {
            set(ChatState.enabled())
        } else if (capabilities.isEnabled(Capability.PREVIEW_ENDED_CAPABILITY)) {
            set(ChatState.disabled(ChatDisabledReason.PREVIEW_ENDED))
        } else if (isLoggedIn) {
            set(ChatState.disabled(ChatDisabledReason.FEATURE_REQUIRED))
        } else {
            set(ChatState.disabled(ChatDisabledReason.AUTHENTICATION_REQUIRED))
        }
    }

    override fun useState(
        parent: Disposable,
        onStateChanged: (state: ChatState) -> Unit
    ) {
        onChange(
            parent,
            ChatEnabledChanged {
                onStateChanged(it)
            }
        )
    }
}

fun interface ChatEnabledChanged : Consumer<ChatState>
