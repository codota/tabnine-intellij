package com.tabnineSelfHosted

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.tabnineCommon.binary.requests.login.LoginRequest
import com.tabnineCommon.binary.requests.login.LogoutRequest
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import com.tabnineSelfHosted.statusBar.LOGIN_TEXT
import com.tabnineSelfHosted.statusBar.LOGOUT_TEXT

class LoginLogoutAction : AnAction() {

    private var isLoggedIn = false
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    init {
        BinaryStateSingleton.instance.onChange(
            BinaryStateSingleton.OnChange { s ->
                this.isLoggedIn = s.isLoggedIn ?: false
            }
        )
    }

    override fun actionPerformed(e: AnActionEvent) = if (this.isLoggedIn) {
        logoutAction()
    } else {
        loginAction()
    }

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.text = if (isLoggedIn) LOGOUT_TEXT else LOGIN_TEXT
    }

    private fun loginAction() {
        Logger.getInstance(javaClass).info("Logging in via action")
        binaryRequestFacade.executeRequest(
            LoginRequest { showUserLoggedInNotification() }
        )
    }

    private fun logoutAction() {
        Logger.getInstance(javaClass).info("Logging out via action")
        binaryRequestFacade.executeRequest(
            LogoutRequest()
        )
    }
}
