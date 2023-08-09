package com.tabnineCommon.chat

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.config.StateRequest
import com.tabnineCommon.chat.actions.TabnineActionsGroup
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.ServiceLevel
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants.CENTER

class ChatFrame(val project: Project, val messagesRouter: ChatMessagesRouter) : JPanel(true) {
    init {
        layout = BorderLayout()

        displayChat()
    }

    private fun displayChat() {
        val browser = try {
            ChatBrowser(messagesRouter, project)
        } catch (e: Exception) {
            Logger.getInstance(javaClass).warn("Failed to create browser", e)
            setComponents(
                listOf(
                    Pair(
                        JLabel("Failed to create browser. Check the log for more details", CENTER).apply {
                            border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
                        },
                        BorderLayout.NORTH
                    )
                )
            )

            return
        }

        val stateResponse = DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(StateRequest())
        val ourGroup = TabnineActionsGroup.create(browser, stateResponse.serviceLevel == ServiceLevel.BUSINESS)
        val createActionToolbar =
            ActionManager.getInstance().createActionToolbar("Tabnine chat", ourGroup, true)
        createActionToolbar.setTargetComponent(browser.jbCefBrowser.component)

        setComponents(
            listOf(
                Pair(createActionToolbar.component, BorderLayout.NORTH),
                Pair(browser.jbCefBrowser.component, null)
            )
        )
    }

    private fun setComponents(components: List<Pair<JComponent, String?>>) {
        this.removeAll()
        for ((component, layout) in components) {
            this.add(component, layout)
        }
        this.revalidate()
    }
}
