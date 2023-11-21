package com.tabnine.chat

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.lifecycle.BinaryStateSingleton

class ChatToolWindowListener : ToolWindowManagerListener {
    companion object {
        const val TABNINE_CHAT_TOOL_WINDOW_ID = "Tabnine Chat"
        const val MINIMAL_MS_BETWEEN_FORCE_REFRESH_CAPABILITIES = 2_000
    }

    private var lastForceRefreshCapabilities = System.currentTimeMillis()

    override fun toolWindowShown(id: String, toolWindow: ToolWindow) {
        super.toolWindowShown(id, toolWindow)

        if (id == TABNINE_CHAT_TOOL_WINDOW_ID) {
            handleTabnineChatToolWindowShown()
        }
    }

    private fun handleTabnineChatToolWindowShown() {
        val isLoggedIn =
            BinaryStateSingleton.instance.get()?.isLoggedIn
                ?: false

        if (isLoggedIn && !ChatEnabledState.instance.get().enabled && isTimeForForceRefreshCapabilities()) {
            lastForceRefreshCapabilities = System.currentTimeMillis()

            CapabilitiesService.getInstance().forceRefreshCapabilities()
        }
    }

    private fun isTimeForForceRefreshCapabilities(): Boolean {
        return System.currentTimeMillis() - lastForceRefreshCapabilities > MINIMAL_MS_BETWEEN_FORCE_REFRESH_CAPABILITIES
    }
}
