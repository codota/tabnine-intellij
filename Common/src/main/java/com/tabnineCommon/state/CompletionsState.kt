package com.tabnineCommon.state

import com.tabnineCommon.general.Utils
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object CompletionsState {
    private val isCompletionsEnabled = AtomicBoolean(true)
    private var timer: Future<*>? = null

    fun setCompletionsEnabled(isEnabled: Boolean) {
        isCompletionsEnabled.set(isEnabled)
        CompletionsStateNotifier.publish(isEnabled)

        timer?.cancel(false)

        if (!isCompletionsEnabled()) {
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
        return isCompletionsEnabled.get()
    }
}
