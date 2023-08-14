package com.tabnineCommon.chat

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier

class ChatToolWindowListener : ToolWindowManagerListener {
    companion object {
        const val TABNINE_CHAT_TOOL_WINDOW_ID = "Tabnine Chat"
        const val MINIMAL_MS_BETWEEN_FORCE_REFRESH_CAPABILITIES = 2_000
    }

    private var isLoggedIn = false
    private var lastForceRefreshCapabilities = System.currentTimeMillis()

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
            handleTabnineChatToolWindowShown()
        }
    }

    private fun handleTabnineChatToolWindowShown() {
        if (isLoggedIn && !ChatEnabled.getInstance().enabled && isTimeForForceRefreshCapabilities()) {
            lastForceRefreshCapabilities = System.currentTimeMillis()

            CapabilitiesService.getInstance().forceRefreshCapabilities()
        }
    }

    private fun isTimeForForceRefreshCapabilities(): Boolean {
        return System.currentTimeMillis() - lastForceRefreshCapabilities > MINIMAL_MS_BETWEEN_FORCE_REFRESH_CAPABILITIES
    }
}
