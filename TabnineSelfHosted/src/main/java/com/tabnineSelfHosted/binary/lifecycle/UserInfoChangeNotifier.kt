package com.tabnineSelfHosted.binary.lifecycle

import com.intellij.util.messages.Topic
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse

fun interface UserInfoChangeNotifier {
    companion object {
        val USER_INFO_CHANGED_TOPIC: Topic<UserInfoChangeNotifier> = Topic.create(
            "User Info Changed Notifier",
            UserInfoChangeNotifier::class.java
        )
    }

    fun stateChanged(state: UserInfoResponse)
}
