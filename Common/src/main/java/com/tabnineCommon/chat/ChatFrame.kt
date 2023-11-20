package com.tabnineCommon.chat

import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.chat.actions.TabnineActionsGroup
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

class ChatFrame(
    private val project: Project,
    private val binaryRequestFacade: BinaryRequestFacade,
    private val useChatEnabled: UseChatEnabledState,
    private val isLoggedIn: () -> Boolean
) :
    JPanel(true), Disposable {

    init {
        layout = BorderLayout()

        useChatEnabled.useState(this) { enabled, loading ->
            ApplicationManager.getApplication().invokeLater {
                updateDisplay(enabled, loading)
            }
        }
    }

    private fun updateDisplay(enabled: Boolean, loading: Boolean) {
        if (enabled) {
            displayChat()
        } else if (loading) {
            displayText("Loading...")
        } else {
            displayChatNotEnabled()
        }
    }

    private fun displayChatNotEnabled() {
        setComponents(listOf(Pair(createChatDisabledJPane(isLoggedIn()), BorderLayout.CENTER)))
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
            ChatBrowser.getInstance(project)
        } catch (e: Exception) {
            Logger.getInstance(javaClass).warn("Failed to create browser", e)
            displayBrowserNotAvailable()

            return
        }

        val ourGroup = TabnineActionsGroup.create(browser)
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

    private fun displayBrowserNotAvailable() {
        val action = ActionManager.getInstance().getAction("ChooseRuntime")

        binaryRequestFacade.executeRequest(EventRequest("chat-browser-not-available", mapOf("choose-runtime-available" to (action != null).toString())))

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
                binaryRequestFacade.executeRequest(EventRequest("chat-choose-runtime-clicked", emptyMap()))
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

    interface UseChatEnabledState {
        fun useState(parent: Disposable, onStateChanged: (enabled: Boolean, loading: Boolean) -> Unit)
    }
}
