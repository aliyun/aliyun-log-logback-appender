package com.aliyun.openservices;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class TestAppender {
    Logger log = LoggerFactory.getLogger(TestAppender.class);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testLog() throws Exception{
        log.warn("hello world! ~~~");
    }

}