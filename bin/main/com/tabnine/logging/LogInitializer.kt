package com.tabnine.logging

import com.intellij.openapi.diagnostic.Logger

fun init() {
    val existingFactory = Logger.getFactory()
    Logger.setFactory(TabnineLoggerFactory(existingFactory))
}
