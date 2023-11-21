package com.tabnine.lifecycle.pushtosignin

import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import com.tabnineCommon.lifecycle.CapabilitiesStateSingleton
import com.tabnineCommon.lifecycle.PluginInstalled
import java.util.concurrent.atomic.AtomicBoolean

class PushToSignIn {
    private var started = AtomicBoolean(false)
    private var lastIsLoggedIn: Boolean? = null
    private var lastIsNewInstalled: Boolean? = null

    fun start() {
        if (started.getAndSet(true)) {
            return
        }
        CapabilitiesStateSingleton.instance.useState(
            CapabilitiesStateSingleton.OnChange { state ->
                if (state.isEnabled(Capability.FORCE_REGISTRATION)) {
                    transition()
                }
            }
        )
        BinaryStateSingleton.instance.useState(
            BinaryStateSingleton.OnChange { response ->
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

    private fun transition(state: StateResponse? = null) {
        val loggedIn =
            if (state != null) state.isLoggedIn else BinaryStateSingleton.instance.get()?.isLoggedIn
        val isNewInstallation = PluginInstalled.isNewInstallation
        val forceRegistration =
            CapabilitiesService.getInstance().isCapabilityEnabled(Capability.FORCE_REGISTRATION)

        if (forceRegistration && (loggedIn != lastIsLoggedIn || isNewInstallation != lastIsNewInstalled)) {
            lastIsLoggedIn = loggedIn
            lastIsNewInstalled = isNewInstallation

            when {
                isNewInstallation == true && loggedIn == false -> {
                    presentPopup()
                    presentNotification()
                }

                isNewInstallation == true && loggedIn == true -> {
                    presentGreeting(
                        state
                            ?: BinaryStateSingleton.instance.get()
                    )
                }

                isNewInstallation == false && loggedIn == false -> {
                    presentNotification()
                }
            }
        }
    }
}
