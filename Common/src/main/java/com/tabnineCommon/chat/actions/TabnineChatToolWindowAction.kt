package com.tabnineCommon.chat.actions

import com.intellij.openapi.actionSystem.AnAction
import com.tabnineCommon.chat.ChatBrowser
import javax.swing.Icon

data class TabnineActionRequest(val command: String, private val data: Any? = null)

abstract class TabnineChatAction(
    protected val browser: ChatBrowser,
    text: String,
    description: String? = text,
    icon: Icon? = null
) :
    AnAction(text, description, icon)
