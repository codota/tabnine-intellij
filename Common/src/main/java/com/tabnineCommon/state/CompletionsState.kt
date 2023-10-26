package com.tabnineCommon.state

import com.tabnineCommon.general.Utils
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object CompletionsState {
    private var isCompletionsEnabled = true
    private var timer: Future<*>? = null

    fun setCompletionsEnabled(isEnabled: Boolean) {
        isCompletionsEnabled = isEnabled
        CompletionsStateNotifier.publish(isEnabled)

        timer?.cancel(false)

        if (!isCompletionsEnabled) {
            timer = Utils.executeThread(
                Runnable {
                    setCompletionsEnabled(true)
                },
                1,
                TimeUnit.HOURS
            )
        }
    }

    fun isCompletionsEnabled(): Boolean {
        return isCompletionsEnabled
    }
}
