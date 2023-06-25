package com.tabnine.chat.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser

class ClearConversationAction(browser: JBCefBrowser) :
    TabnineChatAction(browser, "Clear Conversation", "Clear this conversation", AllIcons.Actions.GC) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("clear-conversation"))
    }
}
