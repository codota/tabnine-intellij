package com.tabnineCommon.chat

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier

const val TABNINE_CHAT_TOOL_WINDOW_ID = "Tabnine Chat"

class ChatToolWindowListener() : ToolWindowManagerListener {
    private var isLoggedIn = false

    init {
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(
                BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
                BinaryStateChangeNotifier { isLoggedIn = it.isLoggedIn ?: false }
            )
    }

    override fun toolWindowShown(id: String, toolWindow: ToolWindow) {
        super.toolWindowShown(id, toolWindow)

        if (id == TABNINE_CHAT_TOOL_WINDOW_ID) {
            ApplicationManager.getApplication().executeOnPooledThread {
                handleTabnineChatToolWindowShown()
            }
        }
    }

    private fun handleTabnineChatToolWindowShown() {
        if (isLoggedIn && !ChatEnabled.getInstance().enabled) {
            CapabilitiesService.getInstance().forceRefreshCapabilities()
        }
    }
}
