package com.tabnine.logging

import org.apache.log4j.spi.Filter
import org.apache.log4j.spi.LoggingEvent

class TabnineFilter : Filter() {
    override fun decide(loggingEvent: LoggingEvent): Int {
        return if (loggingEvent.loggerName.startsWith("#com.tabnine")) ACCEPT else DENY
    }
}
