package com.tabnine.lifecycle

import com.tabnineCommon.general.StaticConfig.BINARY_PROMOTION_POLLING_DELAY
import com.tabnineCommon.general.StaticConfig.BINARY_PROMOTION_POLLING_INTERVAL
import com.tabnineCommon.statusBar.StatusBarUpdater
import java.util.Timer
import kotlin.concurrent.timerTask

class BinaryPromotionStatusBarLifecycle(private val statusBarUpdater: StatusBarUpdater) {
    fun poll() {
        Timer().schedule(
            timerTask {
                statusBarUpdater.requestStatusBarMessage()
            },
            BINARY_PROMOTION_POLLING_DELAY, BINARY_PROMOTION_POLLING_INTERVAL
        )
    }
}
