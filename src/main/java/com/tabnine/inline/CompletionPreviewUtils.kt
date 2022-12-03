package com.tabnine.inline

import com.tabnine.prediction.TabNineCompletion

fun hadSuffix(currentCompletion: TabNineCompletion): Boolean {
    return currentCompletion.oldSuffix?.trim()?.isNotEmpty() ?: false
}

fun isSingleLine(currentCompletion: TabNineCompletion): Boolean {
    return !currentCompletion.getSuffix().trim()?.contains("\n") ?: true
}
fun shouldRemoveSuffixIfSingleLine(currentCompletion: TabNineCompletion): Boolean {
    return hadSuffix(currentCompletion) && isSingleLine(currentCompletion)
}
