package com.tabnine.logging

import com.intellij.openapi.diagnostic.DefaultLogger

class TabnineLogger(delegateFactory: Factory, category: String) : DefaultLogger(category) {
    private val tabnineLogDispatcher = TabnineLogDispatcher()
    private val delegate = delegateFactory.getLoggerInstance(category)

    override fun isDebugEnabled(): Boolean {
        return delegate.isDebugEnabled
    }

    override fun debug(message: String) {
        delegate.debug(message)
    }

    override fun debug(t: Throwable?) {
        delegate.debug(t)
        tabnineLogDispatcher.dispatchLog("debug", "")
    }

    override fun debug(message: String, t: Throwable?) {
        delegate.debug(message, t)
        tabnineLogDispatcher.dispatchLog("debug", message)
    }

    override fun info(message: String) {
        delegate.info(message)
    }

    override fun info(message: String, t: Throwable?) {
        delegate.info(message, t)
        tabnineLogDispatcher.dispatchLog("info", message)
    }

    override fun warn(message: String, t: Throwable?) {
        delegate.warn(message, t)
        tabnineLogDispatcher.dispatchLog("warn", message)
    }

    override fun error(message: String, t: Throwable?, vararg details: String?) {
        delegate.error(message, t, *details)
        tabnineLogDispatcher.dispatchLog("error", message)
    }
}
