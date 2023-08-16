package com.tabnineCommon.chat

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier

class ChatToolWindowListener : ToolWindowManagerListener, BinaryStateChangeNotifier {
    companion object {
        const val TABNINE_CHAT_TOOL_WINDOW_ID = "Tabnine Chat"
        const val MINIMAL_MS_BETWEEN_FORCE_REFRESH_CAPABILITIES = 2_000
    }

    @Volatile
    private var isLoggedIn = false
    private var lastForceRefreshCapabilities = System.currentTimeMillis()

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

    override fun stateChanged(state: StateResponse?) {
        isLoggedIn = state?.isLoggedIn ?: false
    }
}
