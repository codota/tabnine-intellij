package com.tabnineCommon.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.TabnineActionRequest
import com.tabnineCommon.chat.actions.TabnineChatAction

data class MoveToViewPayload(private val view: String)

class ChatHistoryAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "History", "History", AllIcons.Vcs.History) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("move-to-view", MoveToViewPayload("history")))
    }
}
