package com.tabnineCommon.chat.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.toolWindowActions.OpenDevToolsAction

class TabnineActionsGroup private constructor() : DefaultActionGroup("Tabnine Chat", false) {
    companion object {
        fun create(browser: ChatBrowser): TabnineActionsGroup {
            val group = TabnineActionsGroup()
            group.add(OpenDevToolsAction(browser))

            return group
        }
    }
}
