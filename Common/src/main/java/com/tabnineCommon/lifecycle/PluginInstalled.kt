package com.tabnineCommon.lifecycle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic

fun interface PluginInstalled {
    companion object {
        private val PLUGIN_INSTALLED_TOPIC = Topic.create(
            "com.tabnine.installed",
            PluginInstalled::class.java
        )
        var isNewInstallation: Boolean? = null // denotes unknown
            set(value) {
                // once toggled to true , don't allow  toggling back to false
                // if null allow toggling to false
                // don't double publish
                if (value == null) {
                    // don't allow resetting
                    return
                }
                if (field != true && field != value) {
                    field = value
                    publish(field!!)
                }
            }

        private fun publish(value: Boolean) {
            ApplicationManager.getApplication()
                ?.messageBus
                ?.syncPublisher(PLUGIN_INSTALLED_TOPIC)
                ?.installedStateChanged(value)
        }

        fun subscribe(subscriber: PluginInstalled): MessageBusConnection? {
            val bus = ApplicationManager.getApplication()?.messageBus?.connect()
            bus?.subscribe(PLUGIN_INSTALLED_TOPIC, subscriber)
            return bus
        }
    }

    fun installedStateChanged(installed: Boolean)
}
