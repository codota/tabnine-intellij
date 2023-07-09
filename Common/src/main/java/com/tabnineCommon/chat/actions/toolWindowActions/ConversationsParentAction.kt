package com.tabnineCommon.chat.actions.toolWindowActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.tabnineCommon.chat.ChatBrowser
import com.tabnineCommon.chat.actions.TabnineChatAction
import java.awt.Point

class ConversationsParentAction(browser: ChatBrowser, private val conversationActions: List<TabnineChatAction>) :
    TabnineChatAction(browser, "Conversation Actions", icon = AllIcons.Actions.ArrowExpand) {
    override fun actionPerformed(e: AnActionEvent) {
        val group = DefaultActionGroup()
        conversationActions.forEach {
            group.add(it)
        }
        showSubMenu(e, group)
    }

    private fun showSubMenu(e: AnActionEvent, group: DefaultActionGroup) {
        val popupMenu = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Conversation Actions", group, e.dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )
        popupMenu.show(RelativePoint(e.inputEvent.component, Point(0, 0)))
    }
}
