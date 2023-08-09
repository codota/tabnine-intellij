package com.tabnineCommon.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class TabnineChatWebViewFactory : ToolWindowFactory, Disposable {

    private var messagesRouter = ChatMessagesRouter()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.component.add(ChatFrame(project, messagesRouter))
    }

    override fun dispose() {
    }
}
