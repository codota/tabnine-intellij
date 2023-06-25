package com.tabnine.chat.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser

data class MoveToViewPayload(private val view: String)

class ChatHistoryAction(browser: JBCefBrowser) :
    TabnineChatAction(browser, "History", "History", AllIcons.Vcs.History) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("move-to-view", MoveToViewPayload("history")))
    }
}
