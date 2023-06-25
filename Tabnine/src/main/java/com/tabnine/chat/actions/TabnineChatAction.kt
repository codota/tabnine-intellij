package com.tabnine.chat.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.jcef.JBCefBrowser
import com.tabnineCommon.general.DependencyContainer
import javax.swing.Icon

data class TabnineActionRequest(val command: String, private val data: Any? = null)

abstract class TabnineChatAction(private val browser: JBCefBrowser, text: String, description: String, icon: Icon) :
    AnAction(text, description, icon) {
    fun sendMessage(message: TabnineActionRequest) {
        val messageJson = DependencyContainer.instanceOfGson().toJson(message)

        Logger.getInstance(javaClass).info("Sending message: $messageJson")

        browser.cefBrowser.executeJavaScript("window.postMessage($messageJson, '*')", "", 0)
    }
}
