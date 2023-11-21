package com.tabnineSelfHosted.chat

import com.intellij.openapi.Disposable
import com.intellij.util.messages.Topic
import com.tabnineCommon.chat.ChatFrame
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.TopicBasedNonNullState
import com.tabnineSelfHosted.binary.lifecycle.UserInfoStateSingleton
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse
import java.util.function.Consumer

class ChatEnabledState private constructor() : ChatFrame.UseChatEnabledState,
    TopicBasedNonNullState<ChatEnabledData, ChatEnabledChanged>(
        ENABLED_TOPIC,
        ChatEnabledData(enabled = false, loading = true)
    ) {

    companion object {
        private val ENABLED_TOPIC: Topic<ChatEnabledChanged> =
            Topic.create("ChatEnabled", ChatEnabledChanged::class.java)

        val instance = ChatEnabledState()
    }

    init {
        UserInfoStateSingleton.instance.useState(this::updateEnabled)
    }

    private fun updateEnabled(userInfo: UserInfoResponse) {
        if (!StaticConfig.getTabnineEnterpriseHost().isPresent) {
            return
        }

        val newEnabled = userInfo.team != null && userInfo.verified

        set(ChatEnabledData(newEnabled, false))
    }

    override fun useState(
        parent: Disposable,
        onStateChanged: (enabled: Boolean, loading: Boolean) -> Unit
    ) {
        useState(
            parent,
            ChatEnabledChanged {
                onStateChanged(it.enabled, it.loading)
            }
        )
    }
}

data class ChatEnabledData(val enabled: Boolean, val loading: Boolean)
fun interface ChatEnabledChanged : Consumer<ChatEnabledData>
