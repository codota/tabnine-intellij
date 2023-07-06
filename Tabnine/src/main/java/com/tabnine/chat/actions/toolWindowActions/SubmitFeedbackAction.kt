package com.tabnine.chat.actions.toolWindowActions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.IconManager
import com.tabnine.chat.ChatBrowser
import com.tabnine.chat.actions.TabnineActionRequest
import com.tabnine.chat.actions.TabnineChatAction

class SubmitFeedbackAction(browser: ChatBrowser) :
    TabnineChatAction(
        browser,
        "Feedback",
        "Submit feedback",
        IconManager.getInstance().getIcon("/icons/feedback.svg", SubmitFeedbackAction::class.java)
    ) {
    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("submit-feedback"))
    }
}
