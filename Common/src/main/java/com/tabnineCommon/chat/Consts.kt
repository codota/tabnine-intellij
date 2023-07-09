package com.tabnineCommon.chat

import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key

object Consts {
    const val CHAT_TOOL_WINDOW_ID = "Tabnine Chat"
    val BROWSER_PROJECT_KEY = Key<ChatBrowser>("com.tabnine.chat.browser")
    val CHAT_ICON = IconLoader.findIcon("/icons/tabnine-tool-window-icon.svg")
}
