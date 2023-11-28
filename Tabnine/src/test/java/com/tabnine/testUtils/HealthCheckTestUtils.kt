package com.tabnine.testUtils

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.general.ServiceLevel
import com.tabnineCommon.lifecycle.BinaryStateSingleton

object HealthCheckTestUtils {
    @JvmStatic
    fun notifyHealthStatus(
        cloudConnectionHealthStatus: CloudConnectionHealthStatus?,
        times: Int
    ) {
        for (i in 0 until times) {
            notifyHealthStatus(cloudConnectionHealthStatus)
        }
    }

    @JvmStatic
    fun notifyHealthStatus(cloudConnectionHealthStatus: CloudConnectionHealthStatus?) {
        BinaryStateSingleton.instance.set(
            StateResponse(
                null,
                null,
                null,
                cloudConnectionHealthStatus!!
            )
        )
    }

    @JvmStatic
    fun notifyStateForWidget(
        serviceLevel: ServiceLevel,
        isLoggedIn: Boolean,
        cloudConnectionHealthStatus: CloudConnectionHealthStatus
    ) {
        BinaryStateSingleton.instance.set(
            StateResponse(
                serviceLevel = serviceLevel,
                isLoggedIn = isLoggedIn,
                cloudConnectionHealthStatus = cloudConnectionHealthStatus
            )
        )
    }
}
