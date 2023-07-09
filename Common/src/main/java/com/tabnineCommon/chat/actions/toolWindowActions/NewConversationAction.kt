package com.tabnineCommon.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.TabnineActionRequest
import com.tabnineCommon.chat.actions.TabnineChatAction

class NewConversationAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "New Conversation", "New conversation", AllIcons.General.Add) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("create-new-conversation"))
    }
}
