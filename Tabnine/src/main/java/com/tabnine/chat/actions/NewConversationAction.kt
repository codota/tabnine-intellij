package com.tabnine.chat.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser

class NewConversationAction(browser: JBCefBrowser) :
    TabnineChatAction(browser, "New Conversation", "New conversation", AllIcons.General.Add) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("create-new-conversation"))
    }
}
