package com.tabnine.logging

import com.intellij.openapi.diagnostic.Logger

class TabnineLoggerFactory(private val factoryDelegate: Logger.Factory) : Logger.Factory {
    override fun getLoggerInstance(category: String): Logger {
        if (category.contains("com.tabnine")) {
            return TabnineLogger(factoryDelegate, category)
        }
        return factoryDelegate.getLoggerInstance(category)
    }
}
