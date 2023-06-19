package com.tabnineCommon.chat

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class TabnineChatWebViewFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatService = ServiceManager.getService(project, TabnineChatService::class.java)
        toolWindow.component.add(chatService.webViewBrowser.component)
    }
}
