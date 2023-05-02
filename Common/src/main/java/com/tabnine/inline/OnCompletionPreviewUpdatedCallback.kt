package com.tabnine.inline

import com.tabnine.prediction.TabNineCompletion

interface OnCompletionPreviewUpdatedCallback {
    fun onCompletionPreviewUpdated(completion: TabNineCompletion)
}
