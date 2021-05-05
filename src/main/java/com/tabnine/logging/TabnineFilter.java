package com.tabnine.logging;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class TabnineFilter extends org.apache.log4j.spi.Filter {

    @Override
    public int decide(LoggingEvent loggingEvent) {
        return loggingEvent.getLoggerName().startsWith("#com.tabnine") ? Filter.ACCEPT : DENY;
    }
}
