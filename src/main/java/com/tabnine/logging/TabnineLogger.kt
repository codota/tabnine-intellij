package com.tabnine.logging

import com.intellij.openapi.diagnostic.DefaultLogger
import org.apache.commons.lang.exception.ExceptionUtils.getStackTrace

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
        tabnineLogDispatcher.dispatchLog("debug", "", stackTraceOf(t))
    }

    override fun debug(message: String, t: Throwable?) {
        delegate.debug(message, t)
        tabnineLogDispatcher.dispatchLog("debug", message, stackTraceOf(t))
    }

    override fun info(message: String) {
        delegate.info(message)
    }

    override fun info(message: String, t: Throwable?) {
        delegate.info(message, t)
        tabnineLogDispatcher.dispatchLog("info", message, stackTraceOf(t))
    }

    override fun warn(message: String, t: Throwable?) {
        delegate.warn(message, t)
        tabnineLogDispatcher.dispatchLog("warn", message, stackTraceOf(t))
    }

    override fun error(message: String, t: Throwable?, vararg details: String?) {
        delegate.error(message, t, *details)
        tabnineLogDispatcher.dispatchLog("error", message, stackTraceOf(t))
    }

    private fun stackTraceOf(t: Throwable?): String? {
        return t?.let { getStackTrace(t) }
    }
}
