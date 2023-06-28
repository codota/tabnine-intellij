package com.tabnine.chat.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.IconManager
import com.intellij.ui.jcef.JBCefBrowser

class SubmitFeedbackAction(browser: JBCefBrowser) :
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
