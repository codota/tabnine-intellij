package com.tabnine.chat.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.jcef.JBCefBrowser

class TabnineActionsGroup private constructor() : DefaultActionGroup("Tabnine Chat", false) {
    companion object {
        fun create(browser: JBCefBrowser): TabnineActionsGroup {
            val group = TabnineActionsGroup()
            group.add(ClearConversationAction(browser))
            group.add(NewConversationAction(browser))
            group.add(ChatHistoryAction(browser))
            group.add(SubmitFeedbackAction(browser))

            return group
        }
    }
}
