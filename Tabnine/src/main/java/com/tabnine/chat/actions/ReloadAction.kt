package com.tabnine.chat.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnine.chat.ChatBrowser

class ReloadAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "Reload Chat", "Reload Chat", AllIcons.Actions.Refresh) {
    override fun actionPerformed(e: AnActionEvent) {
        browser.reload()
    }
}
