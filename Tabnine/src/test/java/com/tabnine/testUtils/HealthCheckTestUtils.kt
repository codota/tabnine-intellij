package com.tabnine.testUtils

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier

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
        ServiceManager
            .messageBus
            .syncPublisher(BinaryStateChangeNotifier.STATE_CHANGED_TOPIC)
            .stateChanged(StateResponse(null, null, null, cloudConnectionHealthStatus!!))
    }
}
