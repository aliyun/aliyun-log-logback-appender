package com.aliyun.openservices.log.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.Assert.assertNotEquals;

import java.util.List;

public class TestAppender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestAppender.class);

    private static void sleep() {
        ProducerConfig producerConfig = new ProducerConfig();
        try {
            Thread.sleep(2 * producerConfig.getLingerMs());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void checkStatusList() {
        sleep();
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusManager statusManager = lc.getStatusManager();
        List<Status> statusList = statusManager.getCopyOfStatusList();
        for (Status status : statusList) {
            int level = status.getLevel();
            assertNotEquals(status.getMessage(), Status.ERROR, level);
            assertNotEquals(status.getMessage(), Status.WARN, level);
        }
    }

    @Test
    public void testLogCommonMessage() {
        LOGGER.warn("This is a test common message logged by logback.");
    }

    @Test
    public void testLogThrowable() {
        MDC.put("MDC_KEY","MDC_VALUE");
        MDC.put("THREAD_ID", String.valueOf(Thread.currentThread().getId()));
        LOGGER.error("This is a test error message logged by logback.",
                new UnsupportedOperationException("Logback UnsupportedOperationException"));
    }
}