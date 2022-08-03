package com.tabnine.state

import com.intellij.ide.util.PropertiesComponent
import com.tabnine.general.Utils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

const val MAX_DAYS_TO_SHOW_SUGGESTION_HINT = 3
const val IS_SUGGESTION_HINT_SHOWN_STORAGE_KEY = "suggestion-hint-tooltip"

class SuggestionHintState(installationTime: String?) {
    private val installationTime: Date?

    init {
        this.installationTime = convertToDate(installationTime)
    }

    val isEligibleForSuggestionHint: Boolean
        get() {
            if (isHintShown) {
                return false
            }
            val daysDiff = Utils.getDaysDiff(Date(), installationTime)
            return daysDiff in 0..MAX_DAYS_TO_SHOW_SUGGESTION_HINT
        }

    fun setHintWasShown() {
        if (isHintShown) {
            return
        }
        PropertiesComponent.getInstance().setValue(IS_SUGGESTION_HINT_SHOWN_STORAGE_KEY, true)
    }

    private val isHintShown: Boolean
        get() = PropertiesComponent.getInstance()
            .getBoolean(IS_SUGGESTION_HINT_SHOWN_STORAGE_KEY, false)
}

fun convertToDate(installationTime: String?): Date? {
    return try {
        installationTime?.let { SimpleDateFormat("yyyy-MM-dd").parse(it) }
    } catch (e: ParseException) {
        null
    }
}
