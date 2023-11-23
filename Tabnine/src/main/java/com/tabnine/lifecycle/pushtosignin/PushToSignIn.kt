package com.tabnine.lifecycle.pushtosignin

import com.tabnineCommon.lifecycle.BinaryStateSingleton
import java.util.concurrent.atomic.AtomicBoolean

class PushToSignIn {
    private var started = AtomicBoolean(false)

    fun start() {
        if (started.getAndSet(true)) {
            return
        }

        PushToSignInState().useState {
            transition(it.isForceRegistration, it.isNewInstallation, it.isLoggedIn)
        }
    }

    private fun transition(
        forceRegistration: Boolean,
        isNewInstallation: Boolean?,
        loggedIn: Boolean?
    ) {

        when {
            !forceRegistration -> return
            isNewInstallation == true && loggedIn == false -> {
                presentPopup()
                presentNotification()
            }

            isNewInstallation == true && loggedIn == true -> {
                presentGreeting(
                    BinaryStateSingleton.instance.get()
                )
            }

            isNewInstallation == false && loggedIn == false -> {
                presentNotification()
            }
        }
    }
}
