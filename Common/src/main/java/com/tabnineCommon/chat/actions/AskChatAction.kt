package com.tabnineCommon.chat.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.tabnineCommon.chat.Consts.CHAT_ICON
import com.tabnineCommon.chat.actions.common.ChatActionCommunicator

data class AskChatPayload(private val input: String)

class AskChatAction private constructor(private val isChatEnabled: () -> Boolean) : AnAction("Ask Tabnine", "Ask tabnine", CHAT_ICON) {
    companion object {
        private const val ID = "com.tabnine.chat.actions.AskChatAction"

        fun register(isChatEnabled: () -> Boolean) {
            val actionManager = ActionManager.getInstance()
            if (actionManager.getAction(ID) != null) {
                Logger.getInstance(AskChatAction::class.java)
                    .debug("AskChatAction is already registered, skipping registration.")
                return
            }

            Logger.getInstance(AskChatAction::class.java).debug("Registering AskChatAction.")
            actionManager.registerAction(ID, AskChatAction(isChatEnabled))
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val result =
            Messages.showInputDialog("What do you have in mind?", "Ask Tabnine", CHAT_ICON)
                .takeUnless { it.isNullOrBlank() }
                ?: return

        ChatActionCommunicator.sendMessageToChat(project, ID, result)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = isChatEnabled()
    }
}
