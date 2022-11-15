package com.tabnine.statusBar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.tabnine.binary.requests.analytics.EventRequest
import com.tabnine.binary.requests.config.ConfigRequest
import com.tabnine.binary.requests.statusBar.ConfigOpenedFromStatusBarRequest
import com.tabnine.general.DependencyContainer
import com.tabnine.general.GettingStartedManager.openPageOnProject

object StatusBarActions {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    @JvmStatic
    fun buildStatusBarActionsGroup(
        project: Project?
    ): DefaultActionGroup {
        val actions = ArrayList<AnAction>()
        actions.add(createOpenHubAction())
        project?.let {
            actions.add(createGettingStartedAction(it))
        }
        return DefaultActionGroup(actions)
    }

    private fun createOpenHubAction(): DumbAwareAction {
        return DumbAwareAction.create(
            "Open Tabnine Hub"
        ) {
            binaryRequestFacade.executeRequest(ConfigRequest())
            binaryRequestFacade.executeRequest(ConfigOpenedFromStatusBarRequest())
        }
    }

    private fun createGettingStartedAction(project: Project): DumbAwareAction {
        return DumbAwareAction.create("Getting Started Guide") {
            openPageOnProject(project)
            binaryRequestFacade.executeRequest(EventRequest("Button Click", null))
        }
    }
}
