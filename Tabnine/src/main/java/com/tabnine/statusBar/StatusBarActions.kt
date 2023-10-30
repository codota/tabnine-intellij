package com.tabnine.statusBar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.tabnine.binary.requests.config.ConfigRequest
import com.tabnine.general.GettingStartedManager
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.state.CompletionsState.isCompletionsEnabled
import com.tabnineCommon.state.CompletionsState.setCompletionsEnabled

const val OPEN_TABNINE_HUB_TEXT = "Open Tabnine Hub"
const val GETTING_STARTED_TEXT = "Getting Started Guide"

const val SNOOZE_COMPLETION_TEXT = "Snooze Tabnine (1h)"
const val DISABLE_SNOOZE_COMPLETION_TEXT = "Resume Tabnine"

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
        actions.add(createSnoozeCompletionsAction())
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

    private fun createSnoozeCompletionsAction(): DumbAwareAction {
        return if (isCompletionsEnabled()) {
            DumbAwareAction.create(
                SNOOZE_COMPLETION_TEXT
            ) {
                setCompletionsEnabled(false)
                trackSnoozeToggled(false)
            }
        } else {
            DumbAwareAction.create(
                DISABLE_SNOOZE_COMPLETION_TEXT
            ) {
                setCompletionsEnabled(true)
                trackSnoozeToggled(true)
            }
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

    private fun trackSnoozeToggled(showCompletions: Boolean) {
        binaryRequestFacade.executeRequest(
            EventRequest(
                "snooze-toggled",
                mapOf("show_completions" to showCompletions.toString())
            )
        )
    }
}
