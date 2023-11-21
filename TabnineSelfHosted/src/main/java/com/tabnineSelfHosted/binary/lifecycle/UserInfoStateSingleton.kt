package com.tabnineSelfHosted.binary.lifecycle

import com.intellij.util.messages.Topic
import com.tabnineCommon.general.TopicBasedState
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse
import java.util.function.Consumer

class UserInfoStateSingleton private constructor() :
    TopicBasedState<UserInfoResponse, UserInfoStateSingleton.OnChange>(
        USER_INFO_CHANGED_TOPIC
    ) {
    companion object {
        private val USER_INFO_CHANGED_TOPIC = Topic.create(
            "User Info Changed Notifier",
            OnChange::class.java
        )

        val instance = UserInfoStateSingleton()
    }

    public fun interface OnChange : Consumer<UserInfoResponse>
}
