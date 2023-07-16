package com.tabnineCommon.chat.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.tabnine.chat.actions.toolWindowActions.ChatSettingsAction
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.toolWindowActions.ChatHistoryAction
import com.tabnineCommon.chat.actions.toolWindowActions.ClearConversationAction
import com.tabnineCommon.chat.actions.toolWindowActions.NewConversationAction
import com.tabnineCommon.chat.actions.toolWindowActions.OpenDevToolsAction
import com.tabnineCommon.chat.actions.toolWindowActions.SubmitFeedbackAction

class TabnineActionsGroup private constructor() : DefaultActionGroup("Tabnine Chat", false) {
    companion object {
        fun create(browser: ChatBrowser, isEnterprise: Boolean): TabnineActionsGroup {
            val group = TabnineActionsGroup()
            group.add(ClearConversationAction(browser))
            group.add(NewConversationAction(browser))
            group.add(ChatHistoryAction(browser))
            group.add(SubmitFeedbackAction(browser))
            if (!isEnterprise) {
                group.add(ChatSettingsAction(browser))
            }
            group.add(OpenDevToolsAction(browser))

            return group
        }
    }
}
