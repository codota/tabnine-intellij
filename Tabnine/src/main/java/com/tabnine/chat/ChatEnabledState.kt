package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.util.messages.Topic
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.ChatFrame
import com.tabnineCommon.lifecycle.BinaryCapabilitiesChangeNotifier
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier
import com.tabnineCommon.lifecycle.BinaryStateService

class ChatEnabledState private constructor() : Disposable, ChatFrame.UseChatEnabledState {
    var enabled = false
        private set
    private var loading = true

    companion object {
        val ENABLED_TOPIC: Topic<ChatEnabledChanged> = Topic.create("ChatEnabled", ChatEnabledChanged::class.java)

        @Volatile
        private var instance: ChatEnabledState? = null

        @JvmStatic
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ChatEnabledState().also { instance = it }
            }
    }

    init {
        updateEnabled()

        val connection = ApplicationManager.getApplication()
            .messageBus
            .connect(this)

        connection.subscribe(
            BinaryCapabilitiesChangeNotifier.CAPABILITIES_CHANGE_NOTIFIER_TOPIC,
            BinaryCapabilitiesChangeNotifier {
                updateEnabled()
            }
        )

        connection.subscribe(
            BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
            BinaryStateChangeNotifier {
                updateEnabled()
            }
        )
    }

    private fun updateEnabled() {
        if (!CapabilitiesService.getInstance().isReady) {
            return
        }

        val currentBinaryState = ServiceManager.getService(BinaryStateService::class.java).lastStateResponse
            ?: return
        val isLoggedIn = currentBinaryState.isLoggedIn ?: return

        val alphaEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)
        val chatCapabilityEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)

        loading = false

        val newEnabled = isLoggedIn && (alphaEnabled || chatCapabilityEnabled)

        if (enabled != newEnabled) {
            enabled = newEnabled
            resolveNewState(newEnabled)
        }
    }

    private fun resolveNewState(enabled: Boolean) {
        ApplicationManager.getApplication().messageBus.syncPublisher(ENABLED_TOPIC).onChange(enabled, false)
    }

    override fun dispose() {
    }

    override fun useState(
        parent: Disposable,
        onStateChanged: (enabled: Boolean, loading: Boolean) -> Unit
    ) {
        onStateChanged(enabled, loading)

        val connection = ApplicationManager.getApplication().messageBus.connect(parent)

        connection.subscribe(
            ENABLED_TOPIC,
            ChatEnabledChanged { enabled, loading ->
                onStateChanged(enabled, loading)
            }
        )
    }
}

fun interface ChatEnabledChanged {
    fun onChange(enabled: Boolean, loading: Boolean)
}
