package com.tabnine.chat.actions

import com.tabnine.chat.ChatEnabledState
import com.tabnineCommon.chat.actions.AbstractTabnineQuickFixAction

class TabnineQuickFixAction : AbstractTabnineQuickFixAction() {
    override fun isChatEnabled() = ChatEnabledState.instance.get().enabled
}
