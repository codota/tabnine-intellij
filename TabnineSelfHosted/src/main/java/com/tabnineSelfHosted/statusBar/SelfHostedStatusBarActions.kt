package com.tabnineSelfHosted.statusBar

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.login.LoginRequest
import com.tabnineCommon.binary.requests.login.LogoutRequest
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.userSettings.AppSettingsConfigurable
import com.tabnineCommon.userSettings.AppSettingsState

const val OPEN_TABNINE_SETTINGS_TEXT = "Open Tabnine settings"
const val LOGIN_TEXT = "Sign in to Tabnine"
const val LOGOUT_TEXT = "Sign out of Tabnine"
const val GOTO_FAQ_TEXT = "Get help"
const val FAQ_URL = "https://support.tabnine.com/hc/en-us/articles/5760725346193-Connectivity-possible-issues"

object SelfHostedStatusBarActions {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    @JvmStatic
    fun buildStatusBarActionsGroup(
        project: Project?,
        loginStatus: UserLoginStatus
    ): DefaultActionGroup {
        val actions = ArrayList<AnAction>()
        if (AppSettingsState.instance.cloud2Url.isNotBlank()) {
            actions.add(
                when (loginStatus) {
                    UserLoginStatus.Unknown -> {
                        createGoToFAQAction()
                    }

                    UserLoginStatus.LoggedIn -> {
                        createLogoutAction()
                    }

                    UserLoginStatus.LoggedOut -> {
                        createLoginAction()
                    }
                }
            )
        }

        project?.let {
            actions.add(createOpenSettingsAction(it))
        }
        return DefaultActionGroup(actions)
    }

    private fun createOpenSettingsAction(project: Project): DumbAwareAction {
        return DumbAwareAction.create(OPEN_TABNINE_SETTINGS_TEXT) {
            Logger.getInstance(javaClass).info("Opening Tabnine settings")
            ShowSettingsUtil.getInstance()
                .editConfigurable(project, AppSettingsConfigurable())
        }
    }

    private fun createLoginAction(): DumbAwareAction {
        return DumbAwareAction.create(LOGIN_TEXT) {
            Logger.getInstance(javaClass).info("Signing in")
            binaryRequestFacade.executeRequest(
                LoginRequest()
            )
        }
    }

    private fun createLogoutAction(): DumbAwareAction {
        return DumbAwareAction.create(LOGOUT_TEXT) {
            Logger.getInstance(javaClass).info("Signing out")
            binaryRequestFacade.executeRequest(
                LogoutRequest()
            )
        }
    }

    private fun createGoToFAQAction(): DumbAwareAction {
        return DumbAwareAction.create(GOTO_FAQ_TEXT) {
            Logger.getInstance(javaClass).info("Sending to FAQ")
            BrowserUtil.open(FAQ_URL)
        }
    }
}
