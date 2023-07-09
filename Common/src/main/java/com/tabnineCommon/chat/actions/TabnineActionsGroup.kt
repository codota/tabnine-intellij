package com.tabnineCommon.chat.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.toolWindowActions.ChatHistoryAction
import com.tabnineCommon.chat.actions.toolWindowActions.ClearConversationAction
import com.tabnineCommon.chat.actions.toolWindowActions.NewConversationAction
import com.tabnineCommon.chat.actions.toolWindowActions.SubmitFeedbackAction

class TabnineActionsGroup private constructor() : DefaultActionGroup("Tabnine Chat", false) {
    companion object {
        fun create(browser: ChatBrowser): TabnineActionsGroup {
            val group = TabnineActionsGroup()
            group.add(ClearConversationAction(browser))
            group.add(NewConversationAction(browser))
            group.add(ChatHistoryAction(browser))
            group.add(SubmitFeedbackAction(browser))
            group.add(ReloadAction(browser))

            return group
        }
    }
}
