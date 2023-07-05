package com.tabnine.chat.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser

class ReloadAction(browser: JBCefBrowser) :
    TabnineChatAction(browser, "Reload Chat", "Reload Chat", AllIcons.Actions.Refresh) {
    override fun actionPerformed(e: AnActionEvent) {
        browser.cefBrowser.reloadIgnoreCache()
    }
}
