package com.tabnine.inline

import com.tabnine.general.SuggestionTrigger

class DefaultCompletionAdjustment() : CompletionAdjustment {

    override val suggestionTrigger: SuggestionTrigger
        get() = SuggestionTrigger.Keystroke
}
