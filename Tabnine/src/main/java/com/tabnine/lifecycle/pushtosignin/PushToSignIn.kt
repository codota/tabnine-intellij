package com.tabnine.lifecycle.pushtosignin

import com.intellij.notification.Notification
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import java.util.concurrent.atomic.AtomicBoolean

class PushToSignIn {
    private var started = AtomicBoolean(false)
    private lateinit var innerState: PushToSignInState
    private var notification: Notification? = null

    fun start() {
        if (started.getAndSet(true)) {
            return
        }

        innerState = PushToSignInState()
        innerState.onChange {
            transition(it.isForceRegistration, it.isNewInstallation, it.isLoggedIn)
        }
    }

    @Synchronized
    private fun transition(
        forceRegistration: Boolean,
        isNewInstallation: Boolean?,
        loggedIn: Boolean?
    ) {
        when {
            !forceRegistration -> return
            isNewInstallation == true && loggedIn == false -> {
                notification?.expire()
                presentPopup()
                notification = presentNotification()
            }

            isNewInstallation == true && loggedIn == true -> {
                notification?.expire()
                presentGreeting(
                    BinaryStateSingleton.instance.get()
                )
            }

            isNewInstallation == false && loggedIn == false -> {
                notification?.expire()
                notification = presentNotification()
            }
        }
    }
}
