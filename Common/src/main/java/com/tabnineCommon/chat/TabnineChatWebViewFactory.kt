package com.tabnineCommon.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class TabnineChatWebViewFactory(private val browser: ChatBrowser) : ToolWindowFactory, Disposable {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.component.add(ChatFrame(browser))
    }

    override fun dispose() {
    }
}
