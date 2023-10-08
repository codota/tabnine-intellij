package com.tabnineCommon.chat.actions.common

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.Consts
import com.tabnineCommon.chat.actions.AskChatPayload
import com.tabnineCommon.chat.actions.TabnineActionRequest
import com.tabnineCommon.general.DependencyContainer

object ChatActionCommunicator {
    fun sendMessageToChat(project: Project, actionId: String, value: String) {
        val browser = ChatBrowser.getInstance(project)
        val ourToolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(Consts.CHAT_TOOL_WINDOW_ID) ?: return

        if (browser.isChatAppAlive()) {
            ourToolWindow.activate {
                submitMessageToChat(browser, project, value)
            }
        } else {
            browser.registerChatAppStartupListener(actionId, project) {
                submitMessageToChat(browser, project, value)
            }
            ourToolWindow.activate(null)
        }
    }

    private fun submitMessageToChat(browser: ChatBrowser, project: Project, result: String) {
        sendMessage(project, TabnineActionRequest("submit-message", AskChatPayload(result)), browser)
    }

    private fun sendMessage(project: Project, message: TabnineActionRequest, browser: ChatBrowser? = null) {
        val chatBrowser = browser ?: ChatBrowser.getInstance(project)
        val messageJson = DependencyContainer.instanceOfGson().toJson(message)

        Logger.getInstance(javaClass).info("Sending message: $messageJson")
        chatBrowser.jbCefBrowser.cefBrowser.executeJavaScript("window.postMessage($messageJson, '*')", "", 0)
    }
}
