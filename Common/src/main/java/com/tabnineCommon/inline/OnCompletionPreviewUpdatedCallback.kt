package com.tabnineCommon.inline

import com.tabnineCommon.prediction.TabNineCompletion

interface OnCompletionPreviewUpdatedCallback {
    fun onCompletionPreviewUpdated(completion: TabNineCompletion)
}
