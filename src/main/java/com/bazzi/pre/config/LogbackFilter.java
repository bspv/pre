package com.bazzi.pre.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LogbackFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        if(iLoggingEvent.getLoggerName().contains("demo-info")){
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}
