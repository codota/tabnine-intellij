package com.tabnine.inline

import com.tabnine.prediction.TabNineCompletion

fun hadSuffix(currentCompletion: TabNineCompletion): Boolean {
    return currentCompletion.oldSuffix?.trim()?.isNotEmpty() ?: false
}
