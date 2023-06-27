package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.tabnine.chat.actions.TabnineActionsGroup
import java.awt.BorderLayout

class TabnineChatWebViewFactory(private val browser: JBCefBrowser) : ToolWindowFactory, Disposable {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val ourGroup = TabnineActionsGroup.create(browser)
        val createActionToolbar =
            ActionManager.getInstance().createActionToolbar("Tabnine chat", ourGroup, true)
        createActionToolbar.setTargetComponent(browser.component)

        toolWindow.component.add(createActionToolbar.component, BorderLayout.NORTH)
        toolWindow.component.add(browser.component)
    }

    override fun dispose() {}
}
