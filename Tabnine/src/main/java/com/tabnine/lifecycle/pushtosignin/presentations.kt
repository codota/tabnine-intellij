package com.tabnine.lifecycle.pushtosignin

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.binary.requests.login.LoginRequest
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.StaticConfig.TABNINE_NOTIFICATION_GROUP

fun sendAnalytics(event: String) {
    val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
    binaryRequestFacade.executeRequest(EventRequest(event, mapOf()))
}

fun presentPopup() {
    if (!ApplicationManager.getApplication().isDispatchThread) {
        ApplicationManager.getApplication().invokeLater {
            presentPopup()
        }
        return
    }

    sendAnalytics("force-registration-popup-displayed")
    val dialogue = SigninDialogue()
    if (dialogue.showAndGet()) {
        sendAnalytics("force-registration-popup-signin-clicked")
        openSigninPage()
    } else {
        sendAnalytics("force-registration-popup-dismissed")
    }
}

fun openSigninPage() {
    sendAnalytics("force-registration-signin")
    val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
    binaryRequestFacade.executeRequest(
        LoginRequest {}
    )
}

fun presentGreeting(state: StateResponse?) {
    if (state?.userName != null) {
        TABNINE_NOTIFICATION_GROUP
            .createNotification("You are signed in as ${state.userName}", NotificationType.INFORMATION)
            .notify(null)
    }
}

fun presentNotification() {
    TABNINE_NOTIFICATION_GROUP
        .createNotification("Please sign in to start using Tabnine", NotificationType.WARNING)
        .addAction(object : AnAction("Sign In") {
            override fun actionPerformed(e: AnActionEvent) {
                openSigninPage()
            }
        }).notify(null)
}
