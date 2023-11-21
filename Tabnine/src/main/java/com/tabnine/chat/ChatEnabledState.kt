package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.util.messages.Topic
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.ChatFrame
import com.tabnineCommon.general.TopicBasedNonNullState
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import com.tabnineCommon.lifecycle.CapabilitiesStateSingleton
import java.util.function.Consumer

class ChatEnabledState private constructor() : ChatFrame.UseChatEnabledState,
    TopicBasedNonNullState<ChatEnabledData, ChatEnabledChanged>(
        ENABLED_TOPIC,
        ChatEnabledData(enabled = false, loading = true)
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

        val loading = false
        val newEnabled = isLoggedIn && (alphaEnabled || chatCapabilityEnabled)

        set(ChatEnabledData(newEnabled, loading))
    }

    override fun useState(
        parent: Disposable,
        onStateChanged: (enabled: Boolean, loading: Boolean) -> Unit
    ) {
        useState(
            parent,
            ChatEnabledChanged {
                onStateChanged(it.enabled, it.loading)
            }
        )
    }
}

data class ChatEnabledData(val enabled: Boolean, val loading: Boolean)
fun interface ChatEnabledChanged : Consumer<ChatEnabledData>
