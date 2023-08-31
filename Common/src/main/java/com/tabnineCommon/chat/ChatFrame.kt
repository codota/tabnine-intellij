package com.tabnineCommon.chat

import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.tabnineCommon.binary.requests.config.StateRequest
import com.tabnineCommon.chat.actions.TabnineActionsGroup
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.ServiceLevel
import com.tabnineCommon.lifecycle.BinaryCapabilitiesChangeNotifier
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.ImageIcon
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
            <p>This issue may arise if your IDE is running on a Java runtime that does not support<br/>the Java Chromium Embedded Framework (JCEF).</p>
            <p>If you wish, you can <a href="https://choose-runtime">click here</a> to install a JCEF-supporting runtime.</p>
            <p><img src="$imgsrc" width="600" /></p>
        """.trimIndent()
        val checkLogPostfix = """
            <p>Please check the log for more information.</p>
        """.trimIndent()

        val postfix = if (action == null) checkLogPostfix else chooseRuntimePostfix

        val text = JEditorPane(
            "text/html",
            """
            <h3 style="font-weight: normal; color: #cc3e44">Failed to open a browser</h3>
            <p>We were not able to launch a browser, which is needed to display Tabnine chat.</p>
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

        val panel = PanelRound().apply {
            background = JBColor(Color(0xe7, 0xe7, 0xe7), Color(0x1e, 0x1e, 0x1e))
            roundBottomLeft = 10
            roundBottomRight = 10
            roundTopLeft = 10
            roundTopRight = 10
            layout = BorderLayout()
            border = BorderFactory.createEmptyBorder(0, 20, 0, 20)
        }

        val innerPanel = PanelRound().apply {
            background = JBColor(Color.WHITE, Color.BLACK)
            roundBottomLeft = 10
            roundBottomRight = 10
            roundTopLeft = 10
            roundTopRight = 10
            layout = BorderLayout()
            border = BorderFactory.createEmptyBorder(0, 20, 10, 20)
        }

        val tabnineIconUrl = javaClass.classLoader.getResource("icons/tabnine-icon-13px.png")
        panel.add(
            JLabel("Tabnine Chat", ImageIcon(tabnineIconUrl), JLabel.LEFT).apply {
                border = BorderFactory.createEmptyBorder(10, 0, 10, 0)
                font = font.deriveFont(16f)
                iconTextGap = 10
            },
            BorderLayout.NORTH
        )
        panel.add(innerPanel)
        innerPanel.add(text, BorderLayout.LINE_START)

        val outerPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(20, 20, 10, 20)
        }

        outerPanel.add(panel)

        setComponents(
            listOf(
                Pair(
                    outerPanel, BorderLayout.SOUTH
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
