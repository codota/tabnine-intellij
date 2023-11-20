package com.tabnineSelfHosted.chat.actions

import com.tabnineCommon.chat.actions.AbstractTabnineQuickFixAction
import com.tabnineSelfHosted.chat.ChatEnabledState

class TabnineQuickFixAction : AbstractTabnineQuickFixAction() {
    override fun isChatEnabled() = ChatEnabledState.getInstance().enabled
}
