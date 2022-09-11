package com.tabnine.inline

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.tabnine.userSettings.AppSettingsState.Companion.instance

object CompletionTracker {
    private val LAST_COMPLETION_REQUEST_TIME = Key.create<Long>("LAST_COMPLETION_REQUEST_TIME")
    private val DEBOUNCE_INTERVAL_MS = instance.debounceTime

    @JvmStatic
    fun calcDebounceTime(editor: Editor): Long {
        val currentTimestamp = System.currentTimeMillis()
        val lastCompletionTimestamp = LAST_COMPLETION_REQUEST_TIME[editor]
        LAST_COMPLETION_REQUEST_TIME[editor] = currentTimestamp
        if (lastCompletionTimestamp != null) {
            val elapsedTimeFromLastEvent = currentTimestamp - lastCompletionTimestamp
            if (elapsedTimeFromLastEvent < DEBOUNCE_INTERVAL_MS) {
                return DEBOUNCE_INTERVAL_MS - elapsedTimeFromLastEvent
            }
        }
        return 0
    }
}
