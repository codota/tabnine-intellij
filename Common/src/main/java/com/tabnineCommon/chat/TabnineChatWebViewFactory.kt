package com.tabnineCommon.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRequestFacade

class TabnineChatWebViewFactory : ToolWindowFactory, Disposable {

    private var messagesRouter = ChatMessagesRouter()
    private val binaryRequestFacade = instanceOfBinaryRequestFacade()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatFrame = ChatFrame(project, messagesRouter, binaryRequestFacade)
        Disposer.register(toolWindow.disposable, chatFrame)
        toolWindow.component.add(chatFrame)
    }

    override fun dispose() {
    }
}
