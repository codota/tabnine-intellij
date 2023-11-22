package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.util.messages.Topic
import com.tabnineCommon.capabilities.CapabilitiesService
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
        updateEnabled()

        BinaryStateSingleton.instance.useState {
            updateEnabled()
        }

        CapabilitiesStateSingleton.instance.useState {
            updateEnabled()
        }
    }

    private fun updateEnabled() {
        if (!CapabilitiesService.getInstance().isReady) {
            return
        }

        val isLoggedIn = BinaryStateSingleton.instance.get()?.isLoggedIn ?: return

        val alphaEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)
        val chatCapabilityEnabled =
            CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)

        val enabled = isLoggedIn && (alphaEnabled || chatCapabilityEnabled)

        if (enabled) {
            set(ChatState.enabled())
        } else if (!isLoggedIn) {
            set(ChatState.disabled(ChatDisabledReason.AUTHENTICATION_REQUIRED))
        } else {
            set(ChatState.disabled(ChatDisabledReason.FEATURE_REQUIRED))
        }
    }

    override fun useState(
        parent: Disposable, onStateChanged: (state: ChatState) -> Unit
    ) {
        useState(parent, ChatEnabledChanged {
            onStateChanged(it)
        })
    }
}

fun interface ChatEnabledChanged : Consumer<ChatState>
