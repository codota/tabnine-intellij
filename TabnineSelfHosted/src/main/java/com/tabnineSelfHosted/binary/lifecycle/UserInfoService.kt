package com.tabnineSelfHosted.binary.lifecycle

import com.intellij.util.concurrency.AppExecutorUtil
import com.tabnineCommon.general.DependencyContainer
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoRequest
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class UserInfoService {
    private val scheduler = AppExecutorUtil.getAppScheduledExecutorService()
    private val updateLoopStarted = AtomicBoolean(false)
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    fun startUpdateLoop() {
        if (updateLoopStarted.getAndSet(true)) {
            return
        }

        scheduler.scheduleWithFixedDelay(Runnable { updateState() }, 5, 2, TimeUnit.SECONDS)
    }

    fun fetchAndGet(): UserInfoResponse? {
        updateState()
        return UserInfoStateSingleton.instance.get()
    }

    fun updateState() {
        val userInfoResponse = binaryRequestFacade.executeRequest(UserInfoRequest())

        if (userInfoResponse != null) {
            UserInfoStateSingleton.instance.set(userInfoResponse)
        }
    }
}
