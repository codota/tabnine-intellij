package com.tabnineCommon.capabilities

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic

fun interface CapabilityNotifier {
    companion object {
        private val CAPABILITY_CHANGED_TOPIC = Topic.create("com.tabnine.capabilties", CapabilityNotifier::class.java)
        fun publish(state: Set<Capability>) {
            ApplicationManager.getApplication()
                .messageBus
                .syncPublisher(CAPABILITY_CHANGED_TOPIC)
                .stateChanged(state)
        }
        fun subscribe(subscriber: CapabilityNotifier): MessageBusConnection {
            val bus = ApplicationManager.getApplication().messageBus.connect()
            bus.subscribe(CAPABILITY_CHANGED_TOPIC, subscriber)
            return bus
        }
    }
    fun stateChanged(state: Set<Capability>)
}
