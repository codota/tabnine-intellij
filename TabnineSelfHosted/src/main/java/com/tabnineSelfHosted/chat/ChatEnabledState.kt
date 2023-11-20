package com.tabnineSelfHosted.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.util.messages.Topic
import com.tabnineCommon.chat.ChatFrame
import com.tabnineCommon.general.StaticConfig
import com.tabnineSelfHosted.binary.lifecycle.UserInfoChangeNotifier
import com.tabnineSelfHosted.binary.lifecycle.UserInfoService
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse

class ChatEnabledState : Disposable, ChatFrame.UseChatEnabledState {
    var enabled = false
        private set
    private var loading = true

    companion object {
        val ENABLED_TOPIC: Topic<ChatEnabledChanged> = Topic.create("ChatEnabled", ChatEnabledChanged::class.java)

        @Volatile
        private var instance: ChatEnabledState? = null

        @JvmStatic
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ChatEnabledState().also { instance = it }
            }
    }

    init {
        updateEnabled(getUserInfo())

        val connection = ApplicationManager.getApplication()
            .messageBus
            .connect(this)

        connection.subscribe(
            UserInfoChangeNotifier.USER_INFO_CHANGED_TOPIC,
            UserInfoChangeNotifier {
                updateEnabled(it)
            }
        )
    }

    private fun updateEnabled(userInfo: UserInfoResponse?) {
        if (userInfo == null || !StaticConfig.getTabnineEnterpriseHost().isPresent) {
            return
        }

        loading = false

        val newEnabled = userInfo.team != null && userInfo.verified

        if (enabled != newEnabled) {
            enabled = newEnabled
            resolveNewState(newEnabled)
        }
    }

    private fun getUserInfo() = ServiceManager.getService(UserInfoService::class.java).lastUserInfoResponse

    private fun resolveNewState(enabled: Boolean) {
        ApplicationManager.getApplication().messageBus.syncPublisher(ENABLED_TOPIC).onChange(enabled, false)
    }

    override fun dispose() {
    }

    override fun useState(
        parent: Disposable,
        onStateChanged: (enabled: Boolean, loading: Boolean) -> Unit
    ) {
        onStateChanged(enabled, loading)

        val connection = ApplicationManager.getApplication().messageBus.connect(parent)

        connection.subscribe(
            ENABLED_TOPIC,
            ChatEnabledChanged { enabled, loading ->
                onStateChanged(enabled, loading)
            }
        )
    }
}

fun interface ChatEnabledChanged {
    fun onChange(enabled: Boolean, loading: Boolean)
}
