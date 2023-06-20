package com.tabnineCommon.lifecycle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic

fun interface PluginInstalled {
    companion object {
        val PLUGIN_INSTALLED_TOPIC = Topic.create(
            "Plugin Installed Notifier",
            PluginInstalled::class.java
        )
        var isNewInstallation: Boolean? = null // denotes unknown
            set(value) {
                // once toggled to true , don't allow  toggling back to false
                // if null allow toggling to false
                // don't double publish
                if (field != true && field != value) {
                    field = value
                    publish()
                }
            }

        private fun publish() {
            isNewInstallation?.let { value ->
                ApplicationManager.getApplication()
                    .messageBus
                    .syncPublisher(PLUGIN_INSTALLED_TOPIC)
                    .installedStateChanged(value)
            }
        }

        fun subscribe(subscriber: PluginInstalled): MessageBusConnection {
            val bus = ApplicationManager.getApplication().messageBus.connect()
            bus.subscribe(PLUGIN_INSTALLED_TOPIC, subscriber)
            return bus
        }
    }

    fun installedStateChanged(installed: Boolean)
}
