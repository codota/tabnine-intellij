package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.lifecycle.BinaryCapabilitiesChangeNotifier

class TabnineChatWebViewFactory : ToolWindowFactory, Disposable {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val connection = ApplicationManager.getApplication()
            .messageBus
            .connect(this)
        connection.subscribe(
                BinaryCapabilitiesChangeNotifier.CAPABILITIES_CHANGE_NOTIFIER_TOPIC,
                BinaryCapabilitiesChangeNotifier {
                    connection.disconnect()
                    loadChatToolWindow(project, toolWindow)
                }
            )
    }

    private fun loadChatToolWindow(project: Project, toolWindow: ToolWindow) {
        val chatEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA) ||
            CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)
        if (!chatEnabled) return

        val chatService = ServiceManager.getService(project, TabnineChatService::class.java)
        toolWindow.component.add(chatService.getBrowser(project).component)
    }

    override fun dispose() {}
}
