package com.tabnineCommon.chat

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability

class TabnineChatWebViewFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA) ||
            CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)

        if (!chatEnabled) return

        val chatService = ServiceManager.getService(project, TabnineChatService::class.java)
        toolWindow.component.add(chatService.getBrowser(project).component)
    }
}
