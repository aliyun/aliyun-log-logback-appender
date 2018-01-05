package com.aliyun.openservices.log.logback;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.producer.LogProducer;
import com.aliyun.openservices.log.producer.ProducerConfig;
import com.aliyun.openservices.log.producer.ProjectConfig;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ProducerConfig: Default value
 * public int packageTimeoutInMS = 3000; 指定被缓存日志的发送超时时间，如果缓存超时，则会被立即发送,单位毫秒
 * public int logsCountPerPackage = 4096; 指定每个缓存的日志包中包含日志数量的最大值,取值为1~4096
 * public int logsBytesPerPackage = 3145728; 指定每个缓存的日志包的大小上限,取值为1~5242880，单位为字节
 * public int memPoolSizeInByte = 104857600; 指定单个Producer实例可以使用的内存的上限,单位字节
 * public int retryTimes = 3; 指定发送失败时重试的次数
 * public int maxIOThreadSizeInPool = 8; 指定I/O线程池最大线程数量，主要用于发送数据到日志服务
 * public int shardHashUpdateIntervalInMS = 600000;
 *
 * @author 铁生
 */
public class LoghubAppender<E> extends UnsynchronizedAppenderBase<E> {

    private ProducerConfig producerConfig = new ProducerConfig();
    private ProjectConfig projectConfig = new ProjectConfig();

    private LogProducer producer;

    private String logstore; //
    private String topic = ""; //

    private String timeZone = "UTC";
    private String timeFormat = "yyyy-MM-dd'T'HH:mmZ";
    private SimpleDateFormat formatter;

    @Override
    public void start() {
        try {
            doStart();
        } catch (Exception e) {
            addError("Failed to start LoghubAppender.", e);
        }
    }

    private void doStart() {
        formatter = new SimpleDateFormat(timeFormat);
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));

        producer = new LogProducer(producerConfig);
        producer.setProjectConfig(projectConfig);

        super.start();
    }

    @Override
    public void stop() {
        try {
            doStop();
        } catch (Exception e) {
            addError("Failed to stop LoghubAppender.", e);
        }
    }

    private void doStop() throws InterruptedException {
        if (!isStarted())
            return;

        super.stop();
        producer.flush();
        Thread.sleep(2 * producerConfig.packageTimeoutInMS);
        producer.close();
    }

    @Override
    public void append(E eventObject) {
        try {
            appendEvent(eventObject);
        } catch (Exception e) {
            addError("Failed to append event.", e);
        }
    }

    private void appendEvent(E eventObject) {
        //init Event Object
        if (!(eventObject instanceof LoggingEvent)) {
            return;
        }
        LoggingEvent event = (LoggingEvent) eventObject;

        List<LogItem> logItems = new ArrayList<LogItem>();
        LogItem item = new LogItem();
        logItems.add(item);
        item.SetTime((int) (event.getTimeStamp() / 1000));
        //item.PushBack("time", formatter.format(new Date(event.getTimeStamp())));
        item.PushBack("level", event.getLevel().toString());
        item.PushBack("thread", event.getThreadName());

        StackTraceElement[] caller = event.getCallerData();
        if (caller != null || caller.length > 0) {
            item.PushBack("location", caller[0].toString());
        }

        item.PushBack("message", event.getFormattedMessage());
        if (event.getThrowableProxy() != null) {
            item.PushBack("exception", fullDump(event.getThrowableProxy().getStackTraceElementProxyArray()));
        }

        producer.send(projectConfig.projectName, logstore, topic, null, logItems);
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
        formatter = new SimpleDateFormat(timeFormat);
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    private String fullDump(StackTraceElementProxy[] stackTraceElementProxyArray) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
            builder.append(CoreConstants.LINE_SEPARATOR);
        }
        return builder.toString();
    }

    public String getLogstore() {
        return logstore;
    }

    public void setLogstore(String logstore) {
        this.logstore = logstore;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        formatter = new SimpleDateFormat(timeFormat);
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    // **** ==- ProjectConfig -== **********************
    public String getProjectName() {
        return projectConfig.projectName;
    }

    public void setProjectName(String projectName) {
        projectConfig.projectName = projectName;
    }

    public String getEndpoint() {
        return projectConfig.endpoint;
    }

    public void setEndpoint(String endpoint) {
        projectConfig.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return projectConfig.accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        projectConfig.accessKeyId = accessKeyId;
    }

    public String getAccessKey() {
        return projectConfig.accessKey;
    }

    public void setAccessKey(String accessKey) {
        projectConfig.accessKey = accessKey;
    }

    public String getStsToken() {
        return projectConfig.stsToken;
    }

    public void setStsToken(String stsToken) {
        projectConfig.stsToken = stsToken;
    }

    // **** ==- ProjectConfig (end) -== **********************


    // **** ==- ProducerConfig (end) -== **********************
    public int getPackageTimeoutInMS() {
        return producerConfig.packageTimeoutInMS;
    }

    public void setPackageTimeoutInMS(int packageTimeoutInMS) {
        producerConfig.packageTimeoutInMS = packageTimeoutInMS;
    }

    public int getLogsCountPerPackage() {
        return producerConfig.logsCountPerPackage;
    }

    public void setLogsCountPerPackage(int logsCountPerPackage) {
        producerConfig.logsCountPerPackage = logsCountPerPackage;
    }

    public int getLogsBytesPerPackage() {
        return producerConfig.logsBytesPerPackage;
    }

    public void setLogsBytesPerPackage(int logsBytesPerPackage) {
        producerConfig.logsBytesPerPackage = logsBytesPerPackage;
    }

    public int getMemPoolSizeInByte() {
        return producerConfig.memPoolSizeInByte;
    }

    public void setMemPoolSizeInByte(int memPoolSizeInByte) {
        producerConfig.memPoolSizeInByte = memPoolSizeInByte;
    }

    public int getMaxIOThreadSizeInPool() {
        return producerConfig.maxIOThreadSizeInPool;
    }

    public void setMaxIOThreadSizeInPool(int ioThreadsCount) {
        producerConfig.maxIOThreadSizeInPool = ioThreadsCount;
    }

    public int getShardHashUpdateIntervalInMS() {
        return producerConfig.shardHashUpdateIntervalInMS;
    }

    public void setShardHashUpdateIntervalInMS(int shardHashUpdateIntervalInMS) {
        producerConfig.shardHashUpdateIntervalInMS = shardHashUpdateIntervalInMS;
    }

    public int getRetryTimes() {
        return producerConfig.retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        producerConfig.retryTimes = retryTimes;
    }
    // **** ==- ProducerConfig (end) -== **********************

}
