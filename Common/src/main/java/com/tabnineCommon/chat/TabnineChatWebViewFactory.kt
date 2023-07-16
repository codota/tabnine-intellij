package com.tabnineCommon.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.tabnineCommon.binary.requests.config.StateRequest
import com.tabnineCommon.chat.actions.TabnineActionsGroup
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.ServiceLevel
import java.awt.BorderLayout

class TabnineChatWebViewFactory(private val browser: ChatBrowser) : ToolWindowFactory, Disposable {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val stateResponse = DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(StateRequest())
        val ourGroup = TabnineActionsGroup.create(browser, stateResponse.serviceLevel == ServiceLevel.BUSINESS)
        val createActionToolbar =
            ActionManager.getInstance().createActionToolbar("Tabnine chat", ourGroup, true)
        createActionToolbar.setTargetComponent(browser.jbCefBrowser.component)

        toolWindow.component.add(createActionToolbar.component, BorderLayout.NORTH)
        toolWindow.component.add(browser.jbCefBrowser.component)
    }

    override fun dispose() {
    }
}
