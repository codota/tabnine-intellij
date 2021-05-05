package com.tabnine.logging;

import io.sentry.Sentry;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class SentryAppender extends AppenderSkeleton {

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (loggingEvent.getThrowableInformation() != null) {
            Sentry.captureException(loggingEvent.getThrowableInformation().getThrowable());
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
