package com.tabnineSelfHosted

import com.tabnineCommon.capabilities.ISuggestionsModeService
import com.tabnineCommon.capabilities.SuggestionsMode

class SuggestionsModeService : ISuggestionsModeService {
    override fun getSuggestionMode(): SuggestionsMode {
        return SuggestionsMode.INLINE
    }
}
