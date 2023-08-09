package com.tabnineCommon.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class TabnineChatWebViewFactory : ToolWindowFactory, Disposable {

    private var messagesRouter = ChatMessagesRouter()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatFrame = ChatFrame(project, messagesRouter)
        Disposer.register(toolWindow.disposable, chatFrame)
        toolWindow.component.add(chatFrame)
    }

    override fun dispose() {
    }
}
