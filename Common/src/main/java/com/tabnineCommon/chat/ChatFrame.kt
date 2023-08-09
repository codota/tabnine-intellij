package com.tabnineCommon.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.tabnineCommon.binary.requests.config.StateRequest
import com.tabnineCommon.chat.actions.TabnineActionsGroup
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.ServiceLevel
import com.tabnineCommon.lifecycle.BinaryCapabilitiesChangeNotifier
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants.CENTER

class ChatFrame(private val project: Project, private val messagesRouter: ChatMessagesRouter) : JPanel(true), Disposable {
    private var capabilitiesFetched = false

    init {
        layout = BorderLayout()

        updateDisplay()

        val connection = ApplicationManager.getApplication()
            .messageBus
            .connect(this)
        connection.subscribe(
            ChatEnabled.ENABLED_TOPIC,
            ChatEnabledChanged {
                ApplicationManager.getApplication().invokeLater {
                    updateDisplay()
                }
            }
        )

        if (!Config.IS_SELF_HOSTED) {
            connection.subscribe(
                BinaryCapabilitiesChangeNotifier.CAPABILITIES_CHANGE_NOTIFIER_TOPIC,
                BinaryCapabilitiesChangeNotifier {
                    if (!capabilitiesFetched) {
                        capabilitiesFetched = true
                        ApplicationManager.getApplication().invokeLater {
                            updateDisplay()
                        }
                    }
                }
            )
        }
    }

    private fun updateDisplay() {
        if (ChatEnabled.getInstance().enabled) {
            displayChat()
        } else {
            if (capabilitiesFetched || Config.IS_SELF_HOSTED) {
                displayChatNotEnabled()
            } else {
                displayText("Loading...")
            }
        }
    }

    private fun displayChatNotEnabled() {
        displayText("Chat is not enabled")
    }

    private fun displayText(text: String) {
        setComponents(
            listOf(
                Pair(
                    JLabel(text, CENTER).apply {
                        border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
                    },
                    BorderLayout.NORTH
                )
            )
        )
    }

    private fun displayChat() {
        val browser = try {
            ChatBrowser(messagesRouter, project)
        } catch (e: Exception) {
            Logger.getInstance(javaClass).warn("Failed to create browser", e)
            displayText("Failed to create browser. Check the log for more details")

            return
        }

        val stateResponse = DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(StateRequest())
        val ourGroup = TabnineActionsGroup.create(browser, stateResponse?.serviceLevel == ServiceLevel.BUSINESS)
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

    override fun dispose() {
    }
}
