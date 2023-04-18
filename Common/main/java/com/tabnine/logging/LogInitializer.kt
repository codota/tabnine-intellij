package com.tabnine.logging

import com.intellij.openapi.diagnostic.Logger

fun initTabnineLogger() {
    val existingFactory = Logger.getFactory()
    Logger.setFactory(TabnineLoggerFactory(existingFactory))
}
