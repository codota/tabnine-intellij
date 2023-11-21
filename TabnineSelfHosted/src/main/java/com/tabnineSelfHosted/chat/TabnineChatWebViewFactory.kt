package com.tabnineSelfHosted.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.tabnineCommon.chat.ChatFrame
import com.tabnineCommon.general.DependencyContainer.instanceOfBinaryRequestFacade
import com.tabnineSelfHosted.binary.lifecycle.UserInfoStateSingleton

class TabnineChatWebViewFactory : ToolWindowFactory, Disposable {
    private val binaryRequestFacade = instanceOfBinaryRequestFacade()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatFrame = ChatFrame(project, binaryRequestFacade, ChatEnabledState.instance) {
            UserInfoStateSingleton.instance.get()?.isLoggedIn == true
        }
        Disposer.register(toolWindow.disposable, chatFrame)
        toolWindow.component.add(chatFrame)
    }

    override fun dispose() {
    }
}
