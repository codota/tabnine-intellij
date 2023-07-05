package com.tabnine.chat.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.diagnostic.Logger
import com.tabnine.chat.ChatBrowser
import com.tabnineCommon.general.DependencyContainer
import javax.swing.Icon

data class TabnineActionRequest(val command: String, private val data: Any? = null)

abstract class TabnineChatAction(
    protected val browser: ChatBrowser,
    text: String,
    description: String? = text,
    icon: Icon? = null
) :
    AnAction(text, description, icon) {
    fun sendMessage(message: TabnineActionRequest) {
        val messageJson = DependencyContainer.instanceOfGson().toJson(message)

        Logger.getInstance(javaClass).info("Sending message: $messageJson")
        browser.jbCefBrowser.cefBrowser.executeJavaScript("window.postMessage($messageJson, '*')", "", 0)
    }
}
