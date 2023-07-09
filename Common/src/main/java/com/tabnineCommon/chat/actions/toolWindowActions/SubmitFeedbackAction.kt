package com.tabnineCommon.chat.actions.toolWindowActions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.IconManager
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.TabnineActionRequest
import com.tabnineCommon.chat.actions.TabnineChatAction

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
