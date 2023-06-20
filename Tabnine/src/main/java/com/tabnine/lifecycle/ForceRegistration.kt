package com.tabnine.lifecycle

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.messages.MessageBusConnection
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.binary.requests.login.LoginRequest
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.capabilities.CapabilityNotifier
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.StaticConfig.NOTIFICATION_ICON
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier
import com.tabnineCommon.lifecycle.BinaryStateService
import com.tabnineCommon.lifecycle.PluginInstalled
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

class ForceRegistration() {

    private val capabilities: MessageBusConnection

    private val binaryState: MessageBusConnection
    fun start() {
        println("starting force registration flow")
        if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.FORCE_REGISTRATION)) {
            print("force registration capability is enabled so invoking")
            invoke()
        }
    }

    private fun sendAnalytics(event: String) {
        // find the code that sends the analytics
    }

    private fun presentPopup() {
        println("present popup called from thead ${Thread.currentThread()}" )
        if (!ApplicationManager.getApplication().isDispatchThread) {
            println("thread isn't dispatch thread so invoking later")
            ApplicationManager.getApplication().invokeLater {
                println("got invoked")
                presentPopup()
            }
            return
        }

        println("showing popup")
        sendAnalytics("force-registration-popup-displayed")
        val dialogue = SigninDialogue()
        if (dialogue.showAndGet()) {
            sendAnalytics("force-registration-popup-signin-clicked")
            openSigninPage()
        } else {
            sendAnalytics("force-registration-popup-dismissed")
        }
    }

    private fun openSigninPage() {
        sendAnalytics("force-registration-signin")
        val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
        binaryRequestFacade.executeRequest(
            LoginRequest {}
        )
    }

    private fun presentGreeting(state: StateResponse?) {
        println("presenting greeting")
        println("user name is ${state?.userName}")
        if (state?.userName != null) {
            val notification = Notification("force-registration-greeting", NOTIFICATION_ICON, NotificationType.INFORMATION)
            notification.setContent("You are signed in as ${state.userName}")
            notification.addAction(object: AnAction("Ok") {
                override fun actionPerformed(e: AnActionEvent) {
                    notification.expire()
                }
            })
            Notifications.Bus.notify(notification)
        }

    }

    private fun presentNotification() {
        println("presenting notification")
        val notification = Notification("force-signin-id", NOTIFICATION_ICON, NotificationType.INFORMATION)
        notification.setContent("Please sign in to start using Tabnine")
        notification.isImportant = true
        notification.addAction(object: AnAction("Sign in") {
            override fun actionPerformed(e: AnActionEvent) {
                notification.expire()
                openSigninPage()
            }
        })
        Notifications.Bus.notify(notification)

    }

    private fun subscribeLogin() {
        println("subscribing to login")
        val bus = ApplicationManager.getApplication().messageBus.connect()
        bus.subscribe(
            BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
            BinaryStateChangeNotifier { response ->
                if (response.isLoggedIn == true) {
                    bus.disconnect()
                    presentGreeting(response)
                }
            }
        )
    }
    private fun invoke(state: StateResponse? = null) {
        println("invoke called")
        if (!CapabilitiesService.getInstance().isCapabilityEnabled(Capability.FORCE_REGISTRATION)) {
            println("force registration is not set so returning")
            return
        }
        // ServiceManager.getService(BinaryStateService.class).getLastStateResponse();
        val loggedIn = if (state != null) state.isLoggedIn else ServiceManager.getService(BinaryStateService::class.java).lastStateResponse?.isLoggedIn

        val isNewInstallation = PluginInstalled.isNewInstallation
        println("loggedIn = $loggedIn, isNewInstallation = $isNewInstallation" )
        when {

            isNewInstallation == true && loggedIn == false -> {
//                subscribeLogin()
                presentPopup()
                presentNotification()

            }
            isNewInstallation == true && loggedIn == true-> {
                presentGreeting(state ?: ServiceManager.getService(BinaryStateService::class.java).lastStateResponse)
            }
            isNewInstallation == false && loggedIn == false -> {
//                subscribeLogin()
                presentNotification()
            }
            isNewInstallation == false && loggedIn == true -> {
                presentNotification()
            }
        }
    }
    init {
        println("subscribing to capabilities")
        var capCounter = 0
        this.capabilities = CapabilityNotifier.subscribe(
            CapabilityNotifier { state ->
                capCounter += 1
                if (state.contains(Capability.FORCE_REGISTRATION)) {
                    println("capaiblities changed with force registration $capCounter times. invoking")
                    invoke()
                } else {
                    println("capabiltiies called but doesn't have force registration")
                }
            }
        )
        println("subscribing to state change")
        var counter = 0
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(
            BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
            BinaryStateChangeNotifier { response ->
                counter += 1
                println("binary state changed called $counter times. invoking")
                invoke(response)
            }
        )
        this.binaryState = connection
    }
}

class SigninDialogue : DialogWrapper(false) {
    // maybe if you hold soemthing it iwll work?

    val title : JTextArea
    init {
        title = JTextArea("Please sign in to start using Tabnine")
        title.isEditable = false
        init()
    }
    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
//        panel.setSize(1000, 1000)
        panel.add(title)

        return panel
    }
}
