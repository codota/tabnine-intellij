package com.tabnine.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnine.chat.ChatBrowser
import com.tabnine.chat.actions.TabnineActionRequest
import com.tabnine.chat.actions.TabnineChatAction

class NewConversationAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "New Conversation", "New conversation", AllIcons.General.Add) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("create-new-conversation"))
    }
}
