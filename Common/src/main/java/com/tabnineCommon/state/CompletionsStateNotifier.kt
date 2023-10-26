package com.tabnineCommon.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic

interface CompletionsStateNotifier {
    fun stateChanged(isEnabled: Boolean)

    companion object {
        private val COMPLETIONS_STATE_CHANGED_TOPIC = Topic.create("Completions State Changed Notifier", CompletionsStateNotifier::class.java)
        fun publish(isEnabled: Boolean) {
            ApplicationManager.getApplication()?.messageBus
                ?.syncPublisher(COMPLETIONS_STATE_CHANGED_TOPIC)
                ?.stateChanged(isEnabled)
        }

        fun subscribe(subscriber: CompletionsStateNotifier): MessageBusConnection? {
            val bus = ApplicationManager.getApplication()?.messageBus?.connect()
            bus?.subscribe(COMPLETIONS_STATE_CHANGED_TOPIC, subscriber)
            return bus
        }
    }
}
