package com.tabnineSelfHosted.statusBar

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.tabnineSelfHosted.TabnineEnterprisePluginInstaller
import com.tabnineSelfHosted.dialogs.TabnineEnterpriseUrlDialogWrapper
import com.tabnineSelfHosted.userSettings.AppSettingsState

object TabnineSelfHostedForMarketplaceStatusBarActions {
    private var update: (() -> Unit)? = null

    @JvmStatic
    fun buildStatusBarActionsGroup(
        project: Project?,
        update: () -> Unit,
    ): DefaultActionGroup {
        this.update = update
        val actions = project?.let {
            listOf(createAddServerUrlDialogAction(it))
        } ?: emptyList()
        return DefaultActionGroup(actions)
    }

    private fun createAddServerUrlDialogAction(project: Project): DumbAwareAction {
        return DumbAwareAction.create("Open Tabnine server URL dialog") {
            Logger.getInstance(javaClass).info("Open Tabnine server url dialog")
            val dialog = TabnineEnterpriseUrlDialogWrapper(AppSettingsState.instance.cloud2Url)
            if (dialog.showAndGet()) {
                val url = dialog.inputData
                AppSettingsState.instance.cloud2Url = url
                TabnineEnterprisePluginInstaller().installTabnineEnterprisePlugin(null)
                update?.let { f -> f() }
            }
        }
    }
}
