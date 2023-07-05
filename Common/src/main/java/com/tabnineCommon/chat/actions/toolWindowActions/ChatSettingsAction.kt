package com.tabnine.chat.actions.toolWindowActions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.IconManager
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.TabnineActionRequest
import com.tabnineCommon.chat.actions.TabnineChatAction
import com.tabnineCommon.chat.actions.toolWindowActions.MoveToViewPayload

class ChatSettingsAction(browser: ChatBrowser) :
    TabnineChatAction(
        browser,
        "Settings",
        "Settings",
        IconManager.getInstance().getIcon("/icons/settings.svg", ChatSettingsAction::class.java)
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        sendMessage(TabnineActionRequest("move-to-view", MoveToViewPayload("settings")))
    }
}
