package com.tabnine.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnine.chat.ChatBrowser
import com.tabnine.chat.actions.TabnineActionRequest
import com.tabnine.chat.actions.TabnineChatAction

class ClearConversationAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "Clear Conversation", "Clear this conversation", AllIcons.Actions.GC) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("clear-conversation"))
    }
}
