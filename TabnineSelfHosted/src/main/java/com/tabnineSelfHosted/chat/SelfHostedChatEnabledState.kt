package com.tabnineSelfHosted.chat

import com.intellij.openapi.Disposable
import com.intellij.util.messages.Topic
import com.tabnineCommon.chat.ChatDisabledReason
import com.tabnineCommon.chat.ChatFrame
import com.tabnineCommon.chat.ChatState
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.TopicBasedNonNullState
import com.tabnineSelfHosted.binary.lifecycle.UserInfoStateSingleton
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse
import java.util.function.Consumer

class SelfHostedChatEnabledState private constructor() : ChatFrame.UseChatEnabledState,
    TopicBasedNonNullState<ChatState, ChatEnabledChanged>(
        ENABLED_TOPIC, ChatState.loading()
    ) {

    companion object {
        private val ENABLED_TOPIC: Topic<ChatEnabledChanged> =
            Topic.create("ChatEnabled", ChatEnabledChanged::class.java)

        val instance = SelfHostedChatEnabledState()
    }

    init {
        UserInfoStateSingleton.instance.onChange(this::updateEnabled)
    }

    private fun updateEnabled(userInfo: UserInfoResponse) {
        if (!StaticConfig.getTabnineEnterpriseHost().isPresent) {
            return
        }

        if (userInfo.team != null) {
            set(ChatState.enabled())
        } else if (!userInfo.isLoggedIn) {
            set(ChatState.disabled(ChatDisabledReason.AUTHENTICATION_REQUIRED))
        } else {
            set(ChatState.disabled(ChatDisabledReason.PART_OF_A_TEAM_REQUIRED))
        }
    }

    override fun useState(
        parent: Disposable,
        onStateChanged: (state: ChatState) -> Unit
    ) {
        onChange(
            parent,
            ChatEnabledChanged {
                onStateChanged(it)
            }
        )
    }
}

fun interface ChatEnabledChanged : Consumer<ChatState>
