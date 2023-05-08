package com.tabnineSelfHosted.statusBar

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

const val OPEN_TABNINE_SETTINGS_TEXT = "Open Tabnine settings"
const val LOGIN_TEXT = "Sign in to Tabnine"
const val LOGOUT_TEXT = "Sign out of Tabnine"

object SelfHostedStatusBarActions {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    @JvmStatic
    fun buildStatusBarActionsGroup(
        project: Project?,
        isLoggedIn: Boolean
    ): DefaultActionGroup {
        val actions = ArrayList<AnAction>()
        actions.add(
            if (isLoggedIn) {
                createLogoutAction()
            } else {
                createLoginAction()
            }
        )

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
}
