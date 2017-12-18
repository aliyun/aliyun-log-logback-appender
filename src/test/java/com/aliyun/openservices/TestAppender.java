package com.aliyun.openservices;

import com.aliyun.openservices.log.producer.ProducerConfig;
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
        try {
            ProducerConfig config = new ProducerConfig();
            Thread.sleep(2 * config.packageTimeoutInMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("done");
    }

    @Test
    public void testException() throws Exception{
        try{
            double a = 1/0;
        }catch (Exception e){
            log.warn("system error. "+e.getMessage(), e);
        }


        try {
            ProducerConfig config = new ProducerConfig();
            Thread.sleep(2 * config.packageTimeoutInMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("done");
    }
}