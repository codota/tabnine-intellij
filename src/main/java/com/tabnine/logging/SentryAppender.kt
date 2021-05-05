package com.tabnine.logging

import io.sentry.Sentry
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent

class SentryAppender : AppenderSkeleton() {
    override fun append(loggingEvent: LoggingEvent) {
        if (loggingEvent.throwableInformation != null) {
            Sentry.captureException(loggingEvent.throwableInformation.throwable)
        }
    }

    override fun close() {}
    override fun requiresLayout(): Boolean {
        return false
    }
}
