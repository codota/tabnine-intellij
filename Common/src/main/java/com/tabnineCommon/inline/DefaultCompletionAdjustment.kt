package com.tabnineCommon.inline

import com.tabnineCommon.general.SuggestionTrigger

class DefaultCompletionAdjustment : CompletionAdjustment() {

    override val suggestionTrigger: SuggestionTrigger
        get() = SuggestionTrigger.DocumentChanged
}
