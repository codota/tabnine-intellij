package com.tabnineCommon.chat.actions.common

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.Consts
import com.tabnineCommon.chat.actions.AskChatPayload
import com.tabnineCommon.chat.actions.TabnineActionRequest
import com.tabnineCommon.general.DependencyContainer
import org.jetbrains.concurrency.runAsync

object ChatActionCommunicator {
    fun sendMessageToChat(project: Project, actionId: String, value: String) {
        val browser = getBrowser(project) ?: return
        val ourToolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(Consts.CHAT_TOOL_WINDOW_ID) ?: return

        if (browser.isLoaded()) {
            ourToolWindow.activate {
                submitMessageToChat(project, value)
            }
        } else {
            browser.registerBrowserLoadedListener(actionId) {
                runAsync {
                    Thread.sleep(1000)
                    submitMessageToChat(project, value)
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
