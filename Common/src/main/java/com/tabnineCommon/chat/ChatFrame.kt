package com.tabnineCommon.chat

import com.intellij.openapi.actionSystem.ActionManager
import com.tabnineCommon.binary.requests.config.StateRequest
import com.tabnineCommon.chat.actions.TabnineActionsGroup
import com.tabnineCommon.general.DependencyContainer
import com.tabnineCommon.general.ServiceLevel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class ChatFrame(browser: ChatBrowser) : JPanel(true) {
    init {
        layout = BorderLayout()

        val stateResponse = DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(StateRequest())
        val ourGroup = TabnineActionsGroup.create(browser, stateResponse.serviceLevel == ServiceLevel.BUSINESS)
        val createActionToolbar =
            ActionManager.getInstance().createActionToolbar("Tabnine chat", ourGroup, true)
        createActionToolbar.setTargetComponent(browser.jbCefBrowser.component)

        setComponents(
            listOf(
                Pair(createActionToolbar.component, BorderLayout.NORTH),
                Pair(browser.jbCefBrowser.component, null)
            )
        )
    }

    fun setComponents(components: List<Pair<JComponent, String?>>) {
        this.removeAll()
        for ((component, layout) in components) {
            this.add(component, layout)
        }
        this.revalidate()
    }
}
