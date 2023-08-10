package com.tabnineCommon.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.lifecycle.BinaryCapabilitiesChangeNotifier

class ChatEnabled : Disposable {
    var enabled: Boolean = false
        private set

    companion object {
        val ENABLED_TOPIC: Topic<ChatEnabledChanged> = Topic.create("ChatEnabled", ChatEnabledChanged::class.java)

        @Volatile
        private var instance: ChatEnabled? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ChatEnabled().also { instance = it }
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
    }

    private fun updateEnabled() {
        val alphaEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)
        val chatCapabilityEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)
        val isSelfHostedWithUrl = Config.IS_SELF_HOSTED && StaticConfig.getTabnineEnterpriseHost().isPresent
        val newEnabled = isSelfHostedWithUrl || chatCapabilityEnabled || alphaEnabled

        if (enabled != newEnabled) {
            enabled = newEnabled
            ApplicationManager.getApplication().messageBus.syncPublisher(ENABLED_TOPIC).notifyChanged()
        }
    }

    override fun dispose() {
    }
}

fun interface ChatEnabledChanged {
    fun notifyChanged()
}
