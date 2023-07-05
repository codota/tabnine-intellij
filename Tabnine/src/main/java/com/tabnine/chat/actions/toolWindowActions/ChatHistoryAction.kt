package com.tabnine.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnine.chat.ChatBrowser
import com.tabnine.chat.actions.TabnineActionRequest
import com.tabnine.chat.actions.TabnineChatAction

data class MoveToViewPayload(private val view: String)

class ChatHistoryAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "History", "History", AllIcons.Vcs.History) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("move-to-view", MoveToViewPayload("history")))
    }
}
