package com.aliyun.openservices.log.logback.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by brucewu on 2018/1/8.
 */
public class LogbackAppenderExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackAppenderExample.class);

    public static void main(String[] args) {

        MDC.put("MDC_KEY","MDC_VALUE");
        MDC.put("THREAD_ID", String.valueOf(Thread.currentThread().getId()));

        LOGGER.trace("trace log");
        LOGGER.debug("debug log");
        LOGGER.info("info log");
        LOGGER.warn("warn log");
        LOGGER.error("error log");
    }

}
