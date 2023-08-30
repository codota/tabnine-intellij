package com.tabnineCommon.chat

import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
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
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants.CENTER
import javax.swing.event.HyperlinkEvent

class ChatFrame(private val project: Project, private val messagesRouter: ChatMessagesRouter) :
    JPanel(true), Disposable {
    private var capabilitiesFetched = false

    init {
        layout = BorderLayout()

        updateDisplay()

        val connection = ApplicationManager.getApplication().messageBus.connect(this)
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
        displayJLabel(createChatDisabledJLabel())
    }

    private fun displayText(text: String) {
        displayJLabel(
            JLabel(text, CENTER).apply {
                border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
            }
        )
    }

    private fun displayJLabel(label: JLabel) {
        setComponents(
            listOf(
                Pair(
                    label, BorderLayout.NORTH
                )
            )
        )
    }

    private fun displayChat() {
        val browser = try {
            ChatBrowser(messagesRouter, project)
        } catch (e: Exception) {
            Logger.getInstance(javaClass).warn("Failed to create browser", e)
            displayBrowserNotEnabled()

            return
        }

        val stateResponse =
            DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(StateRequest())
        val ourGroup = TabnineActionsGroup.create(
            browser, stateResponse?.serviceLevel == ServiceLevel.BUSINESS
        )
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

    private fun displayBrowserNotEnabled() {
        val action = ActionManager.getInstance().getAction("ChooseRuntime")
        val imgsrc = javaClass.classLoader.getResource("images/choose-runtime-with-jcef.png")?.toString()
        val chooseRuntimePostfix = """
            <p>This might be a result of running your IDE under a runtime that does not support the Java Chromium Embedded Framework (JCEF).</p>
            <p>If you wish, you can <a href="https://choose-runtime">click here</a> to install a runtime that supports the JCEF browser</p>
            <p><img src="$imgsrc" width="600" /></p>
        """.trimIndent()
        val checkLogPostfix = """
            <p>Please check the log for more information.</p>
        """.trimIndent()

        val postfix = if (action == null) checkLogPostfix else chooseRuntimePostfix

        val text = JEditorPane(
            "text/html",
            """
            <h3>Failed to open a browser</h3>
            <p>We could not open a browser, which is required to display Tabnine chat.</p>
            $postfix
            """.trimIndent()
        ).apply {
            isEditable = false
            isOpaque = false
        }
        text.addHyperlinkListener {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                action?.actionPerformed(AnActionEvent.createFromInputEvent(null, "TabnineChatFrame", null, DataManager.getInstance().getDataContext(text)))
            }
        }
        setComponents(
            listOf(
                Pair(
                    text, null
                )
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
