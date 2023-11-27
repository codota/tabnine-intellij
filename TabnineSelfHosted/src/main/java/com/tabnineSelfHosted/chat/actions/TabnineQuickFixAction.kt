package com.tabnineSelfHosted.chat.actions

import com.tabnineCommon.chat.actions.AbstractTabnineQuickFixAction
import com.tabnineSelfHosted.chat.SelfHostedChatEnabledState

class TabnineQuickFixAction : AbstractTabnineQuickFixAction() {
    override fun isChatEnabled() = SelfHostedChatEnabledState.instance.get().enabled
}
