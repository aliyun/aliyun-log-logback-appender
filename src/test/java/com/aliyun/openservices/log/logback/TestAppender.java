package com.aliyun.openservices.log.logback;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAppender {
    Logger log = LoggerFactory.getLogger(TestAppender.class);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testLog() throws Exception {
        log.warn("hello world! ~~~");
        System.out.println("done");
    }

    @Test
    public void testException() throws Exception {
        try {
            double a = 1 / 0;
        } catch (Exception e) {
            log.warn("system error. " + e.getMessage(), e);
        }
        System.out.println("done");
    }

    @Test
    public void testMultipleLogs() throws Exception {
        log.warn("hello world1! ~~~");
        log.warn("hello world2! ~~~");
        log.warn("hello world3! ~~~");
        System.out.println("done");
    }
}