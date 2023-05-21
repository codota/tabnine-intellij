package com.tabnineSelfHosted.statusBar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.tabnineSelfHosted.userSettings.AppSettingsConfigurable

object SelfHostedForMarketplaceStatusBarActions {
    @JvmStatic
    fun buildStatusBarActionsGroup(
        project: Project?,
    ): DefaultActionGroup {
        val actions = ArrayList<AnAction>()
        project?.let {
            actions.add(createOpenSettingsAction(it))
        }
        return DefaultActionGroup(actions)
    }

    private fun createOpenSettingsAction(project: Project): DumbAwareAction {
        return DumbAwareAction.create("Open Tabnine settings") {
            Logger.getInstance(javaClass).info("Opening Tabnine settings")
            ShowSettingsUtil.getInstance()
                .editConfigurable(project, AppSettingsConfigurable())
        }
    }
}
