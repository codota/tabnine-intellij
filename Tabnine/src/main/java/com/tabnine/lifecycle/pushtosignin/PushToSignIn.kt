package com.tabnine.lifecycle.pushtosignin
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.capabilities.CapabilityNotifier
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier
import com.tabnineCommon.lifecycle.BinaryStateService
import com.tabnineCommon.lifecycle.PluginInstalled
class PushToSignIn {
    private fun transition(state: StateResponse? = null) {
        val loggedIn = if (state != null) state.isLoggedIn else ServiceManager.getService(BinaryStateService::class.java).lastStateResponse?.isLoggedIn

        val isNewInstallation = PluginInstalled.isNewInstallation
        when {
            !CapabilitiesService.getInstance().isCapabilityEnabled(Capability.FORCE_REGISTRATION) -> return
            isNewInstallation == true && loggedIn == false -> {
                presentPopup()
                presentNotification()
            }
            isNewInstallation == true && loggedIn == true -> {
                presentGreeting(state ?: ServiceManager.getService(BinaryStateService::class.java).lastStateResponse)
            }
            isNewInstallation == false && loggedIn == false -> {
                presentNotification()
            }
        }
    }
    init {
        CapabilityNotifier.subscribe(
            CapabilityNotifier { state ->
                if (state.contains(Capability.FORCE_REGISTRATION)) {
                    transition()
                }
            }
        )
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(
            BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
            BinaryStateChangeNotifier { response ->
                transition(response)
            }
        )

        PluginInstalled.subscribe(
            PluginInstalled { installed ->
                if (installed) {
                    transition()
                }
            }
        )
    }
}
