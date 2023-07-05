package com.tabnine.chat.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.tabnine.chat.ChatBrowser
import com.tabnine.chat.Consts.CHAT_ICON
import com.tabnine.chat.Consts.CHAT_TOOL_WINDOW_ID
import com.tabnineCommon.general.DependencyContainer
import org.jetbrains.concurrency.runAsync

data class AskChatPayload(private val input: String)

class AskChatAction private constructor() : AnAction("Ask Tabnine", "Ask tabnine", CHAT_ICON) {
    companion object {
        private const val ID = "com.tabnine.chat.actions.AskChatAction"

        fun register() {
            val actionManager = ActionManager.getInstance()
            if (actionManager.getAction(ID) != null) {
                Logger.getInstance(AskChatAction::class.java)
                    .debug("AskChatAction is already registered, skipping registration.")
                return
            }

            Logger.getInstance(AskChatAction::class.java).debug("Registering AskChatAction.")
            actionManager.registerAction(ID, AskChatAction())
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val browser = getBrowser(project) ?: return
        val ourToolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(CHAT_TOOL_WINDOW_ID) ?: return

        val result =
            Messages.showInputDialog("What do you have in mind?", "Ask Tabnine", CHAT_ICON)
                .takeUnless { it.isNullOrBlank() }
                ?: return

        if (browser.isLoaded()) {
            ourToolWindow.activate {
                submitMessageToChat(project, result)
            }
        } else {
            browser.registerBrowserLoadedListener(ID) {
                runAsync {
                    Thread.sleep(1000)
                    submitMessageToChat(project, result)
                }
            }
            ourToolWindow.activate(null)
        }
    }

    private fun submitMessageToChat(project: Project, result: String) {
        sendMessage(project, TabnineActionRequest("submit-message", AskChatPayload(result)))
    }

    private fun sendMessage(project: Project, message: TabnineActionRequest) {
        val browser = getBrowser(project) ?: return
        val messageJson = DependencyContainer.instanceOfGson().toJson(message)

        Logger.getInstance(javaClass).info("Sending message: $messageJson")
        browser.jbCefBrowser.cefBrowser.executeJavaScript("window.postMessage($messageJson, '*')", "", 0)
    }

    private fun getBrowser(project: Project): ChatBrowser? {
        val browser = ChatBrowser.getInstance(project)
        if (browser == null) {
            Logger.getInstance(javaClass).warn("Browser not found on project ${project.name}")
            return null
        }
        return browser
    }
}
