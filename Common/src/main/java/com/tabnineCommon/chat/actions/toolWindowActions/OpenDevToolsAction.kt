package com.tabnineCommon.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.TabnineChatAction

class OpenDevToolsAction(browser: ChatBrowser) :
    TabnineChatAction(browser, "Open Devtools", "Open devtools", AllIcons.Toolwindows.ToolWindowDebugger) {
    override fun actionPerformed(e: AnActionEvent) {
        browser.jbCefBrowser.openDevtools()
    }
}
