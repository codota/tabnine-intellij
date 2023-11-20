package com.tabnineSelfHosted.binary.lifecycle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.messages.MessageBus
import com.tabnineCommon.general.DependencyContainer
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoRequest
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class UserInfoService {
    private val scheduler = AppExecutorUtil.getAppScheduledExecutorService()
    private val messageBus: MessageBus = ApplicationManager.getApplication().messageBus
    private val updateLoopStarted = AtomicBoolean(false)
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
    var lastUserInfoResponse: UserInfoResponse? = null

    fun startUpdateLoop() {
        if (updateLoopStarted.getAndSet(true)) {
            return
        }

        scheduler.scheduleWithFixedDelay(Runnable { updateState() }, 5, 2, TimeUnit.SECONDS)
    }

    fun fetchAndGet(): UserInfoResponse? {
        updateState()
        return lastUserInfoResponse
    }

    fun updateState() {
        val userInfoResponse = binaryRequestFacade.executeRequest(UserInfoRequest())
        if (userInfoResponse != null) {
            if (userInfoResponse != lastUserInfoResponse) {
                messageBus
                    .syncPublisher(UserInfoChangeNotifier.USER_INFO_CHANGED_TOPIC)
                    .stateChanged(userInfoResponse)
            }
            this.lastUserInfoResponse = userInfoResponse
        }
    }
}
