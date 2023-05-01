package com.tabnine.statusBar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.binary.requests.config.ConfigRequest
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.GettingStartedManager

const val OPEN_TABNINE_HUB_TEXT = "Open Tabnine Hub"
const val GETTING_STARTED_TEXT = "Getting Started Guide"

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
            OPEN_TABNINE_HUB_TEXT
        ) {
            binaryRequestFacade.executeRequest(ConfigRequest())
            binaryRequestFacade.executeRequest(
                EventRequest(
                    "Button Click",
                    mapOf("action_name" to "open_hub", "action_text" to OPEN_TABNINE_HUB_TEXT)
                )
            )
        }
    }

    private fun createGettingStartedAction(project: Project): DumbAwareAction {
        return DumbAwareAction.create(GETTING_STARTED_TEXT) {
            GettingStartedManager.instance.openPageOnProject(project)
            binaryRequestFacade.executeRequest(
                EventRequest(
                    "Button Click",
                    mapOf("action_name" to "open_getting_started_page", "action_text" to GETTING_STARTED_TEXT)
                )
            )
        }
    }
}
