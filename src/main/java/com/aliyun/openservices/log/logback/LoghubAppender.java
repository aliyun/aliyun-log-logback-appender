package com.aliyun.openservices.log.logback;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.producer.LogProducer;
import com.aliyun.openservices.log.producer.ProducerConfig;
import com.aliyun.openservices.log.producer.ProjectConfig;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

/**
 * ProducerConfig: Default value
 * public int packageTimeoutInMS = 3000; 指定被缓存日志的发送超时时间，如果缓存超时，则会被立即发送,单位毫秒
 * public int logsCountPerPackage = 4096; 指定每个缓存的日志包中包含日志数量的最大值,取值为1~4096
 * public int logsBytesPerPackage = 3145728; 指定每个缓存的日志包的大小上限,取值为1~5242880，单位为字节
 * public int memPoolSizeInByte = 104857600; 指定单个Producer实例可以使用的内存的上限,单位字节
 * public int retryTimes = 3; 指定发送失败时重试的次数
 * public int maxIOThreadSizeInPool = 8; 指定I/O线程池最大线程数量，主要用于发送数据到日志服务
 *
 * @author 铁生
 */
public class LoghubAppender<E> extends UnsynchronizedAppenderBase<E> {

    private Encoder<E> encoder;

    private ProducerConfig producerConfig = new ProducerConfig();
    private ProjectConfig projectConfig = new ProjectConfig();

    private LogProducer producer;

    private String logstore; //
    private String topic = ""; //
    private String source = ""; //

    private String timeZone = "UTC";
    private String timeFormat = "yyyy-MM-dd'T'HH:mmZ";
    private DateTimeFormatter formatter;

    @Override
    public void start() {
        try {
            doStart();
        } catch (Exception e) {
            addError("Failed to start LoghubAppender.", e);
        }
    }

    private void doStart() {
        formatter = DateTimeFormat.forPattern(timeFormat).withZone(DateTimeZone.forID(timeZone));
        producerConfig.userAgent = "logback";
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

        DateTime dateTime = new DateTime(event.getTimeStamp());
        item.PushBack("time", dateTime.toString(formatter));
        item.PushBack("level", event.getLevel().toString());
        item.PushBack("thread", event.getThreadName());

        StackTraceElement[] caller = event.getCallerData();
        if (caller != null && caller.length > 0) {
            item.PushBack("location", caller[0].toString());
        }

        String message = event.getFormattedMessage();
        item.PushBack("message", message);

        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (iThrowableProxy != null) {
            String throwable = getExceptionInfo(iThrowableProxy);
            throwable += fullDump(event.getThrowableProxy().getStackTraceElementProxyArray());
            item.PushBack("throwable", throwable);
        }

        if (this.encoder != null) {
            item.PushBack("log", new String(this.encoder.encode(eventObject)));
        }

        Map<String,String> mdc = event.getMDCPropertyMap();
        if (mdc.size() > 0) {
            for (Map.Entry<String, String> entry : mdc.entrySet()) {
                item.PushBack(entry.getKey(), entry.getValue());
            }
        }

        producer.send(projectConfig.projectName, logstore, topic, source, logItems, new LoghubAppenderCallback<E>(this,
                projectConfig.projectName, logstore, topic, source, logItems));
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    private String getExceptionInfo(IThrowableProxy iThrowableProxy) {
        String s = iThrowableProxy.getClassName();
        String message = iThrowableProxy.getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    private String fullDump(StackTraceElementProxy[] stackTraceElementProxyArray) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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

    public int getRetryTimes() {
        return producerConfig.retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        producerConfig.retryTimes = retryTimes;
    }
    // **** ==- ProducerConfig (end) -== **********************

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }
}
