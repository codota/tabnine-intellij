package com.tabnineCommon.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.TabnineActionRequest
import com.tabnineCommon.chat.actions.TabnineChatAction

class ClearConversationAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "Clear Conversation", "Clear this conversation", AllIcons.Actions.GC) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("clear-conversation"))
    }
}
