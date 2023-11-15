package com.aliyun.openservices.log.logback;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogItem;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;

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

    private String project;

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String userAgent = "logback";

    protected Encoder<E> encoder;

    private boolean includeLocation = true;

    private boolean includeMessage = true;

    protected ProducerConfig producerConfig = new ProducerConfig();
    protected ProjectConfig projectConfig;

    protected Producer producer;

    protected String logStore; //
    protected String topic = ""; //
    protected String source = ""; //

    protected String timeZone = "UTC";
    protected String timeFormat = "yyyy-MM-dd'T'HH:mmZ";
    protected DateTimeFormatter formatter;

    protected java.time.format.DateTimeFormatter formatter1;
    private String mdcFields;

    protected int maxThrowable = 500;

    @Override
    public void start() {
        try {
            doStart();
        } catch (Exception e) {
            addError("Failed to start LoghubAppender.", e);
        }
    }

    private void doStart() {
        try {
            formatter = DateTimeFormat.forPattern(timeFormat).withZone(DateTimeZone.forID(timeZone));
        }catch (Exception e){
            formatter1 = java.time.format.DateTimeFormatter.ofPattern(timeFormat).withZone(ZoneId.of(timeZone));
        }
        producer = createProducer();
        super.start();
    }

    public Producer createProducer() {
        projectConfig = buildProjectConfig();
        Producer producer = new LogProducer(producerConfig);
        producer.putProjectConfig(projectConfig);
        return producer;
    }

    private ProjectConfig buildProjectConfig() {
        return new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret, null, userAgent);
    }

    @Override
    public void stop() {
        try {
            doStop();
        } catch (Exception e) {
            addError("Failed to stop LoghubAppender.", e);
        }
    }

    private void doStop() throws InterruptedException, ProducerException {
        if (!isStarted()) {
            return;
        }

        super.stop();
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

        if(formatter!=null){
            DateTime dateTime = new DateTime(event.getTimeStamp());
            item.PushBack("time", dateTime.toString(formatter));
        }else {
            Instant instant = Instant.ofEpochMilli(event.getTimeStamp());
            item.PushBack("time", formatter1.format(instant));
        }

        item.PushBack("level", event.getLevel().toString());
        item.PushBack("thread", event.getThreadName());

        if (this.includeLocation) {
            StackTraceElement[] caller = event.getCallerData();
            if (caller != null && caller.length > 0) {
                item.PushBack("location", caller[0].toString());
            }
        }

        if (this.encoder == null || this.includeMessage) {
            String message = event.getFormattedMessage();
            item.PushBack("message", message);
        }

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            StringBuilder throwable = new StringBuilder(this.getExceptionInfo(throwableProxy));

            do {
                throwable.append(this.fullDump(throwableProxy.getStackTraceElementProxyArray()));
                throwableProxy = throwableProxy.getCause();
                if (throwableProxy != null) {
                    throwable.append("\n\nCaused by:")
                            .append(this.getExceptionInfo(throwableProxy));
                }
            } while (throwableProxy != null);
            String throwableSub;
            if (throwable.length() > maxThrowable) {
                throwableSub = throwable.substring(0, maxThrowable);
            } else {
                throwableSub = throwable.toString();
            }
            item.PushBack("throwable", throwableSub);
        }

        if (this.encoder != null) {
            item.PushBack("log", new String(this.encoder.encode(eventObject)));
        }

        // mdcFields can be "*" or format of "fieldA,FieldB,fieldC"
        if (mdcFields != null && mdcFields.trim().equals("*")) { // "*" matches all fields, add all fields to item
            event.getMDCPropertyMap().entrySet().forEach(e -> item.PushBack(e.getKey(), e.getValue()));
        } else {
            Optional.ofNullable(mdcFields).ifPresent(
                    f -> event.getMDCPropertyMap().entrySet().stream()
                            .filter(v -> Arrays.stream(f.split(",")).anyMatch(i -> i.equals(v.getKey())))
                            .forEach(map -> item.PushBack(map.getKey(), map.getValue()))
            );
        }


        try {
            producer.send(projectConfig.getProject(), logStore, topic, source, logItems, new LoghubAppenderCallback<E>(this,
                    projectConfig.getProject(), logStore, topic, source, logItems));
        } catch (Exception e) {
        }
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

    public String getLogStore() {
        return logStore;
    }

    public void setLogStore(String logStore) {
        this.logStore = logStore;
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

    public int getMaxThrowable() {
        return maxThrowable;
    }

    public void setMaxThrowable(int maxThrowable) {
        this.maxThrowable = maxThrowable;
    }

    // **** ==- ProjectConfig -== **********************
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getTotalSizeInBytes() {
        return producerConfig.getTotalSizeInBytes();
    }

    public void setTotalSizeInBytes(int totalSizeInBytes) {
        producerConfig.setTotalSizeInBytes(totalSizeInBytes);
    }

    public long getMaxBlockMs() {
        return producerConfig.getMaxBlockMs();
    }

    public void setMaxBlockMs(long maxBlockMs) {
        producerConfig.setMaxBlockMs(maxBlockMs);
    }

    public int getIoThreadCount() {
        return producerConfig.getIoThreadCount();
    }

    public void setIoThreadCount(int ioThreadCount) {
        producerConfig.setIoThreadCount(ioThreadCount);
    }

    public int getBatchSizeThresholdInBytes() {
        return producerConfig.getBatchSizeThresholdInBytes();
    }

    public void setBatchSizeThresholdInBytes(int batchSizeThresholdInBytes) {
        producerConfig.setBatchSizeThresholdInBytes(batchSizeThresholdInBytes);
    }

    public int getBatchCountThreshold() {
        return producerConfig.getBatchCountThreshold();
    }

    public void setBatchCountThreshold(int batchCountThreshold) {
        producerConfig.setBatchCountThreshold(batchCountThreshold);
    }

    public int getLingerMs() {
        return producerConfig.getLingerMs();
    }

    public void setLingerMs(int lingerMs) {
        producerConfig.setLingerMs(lingerMs);
    }

    public int getRetries() {
        return producerConfig.getRetries();
    }

    public void setRetries(int retries) {
        producerConfig.setRetries(retries);
    }

    public int getMaxReservedAttempts() {
        return producerConfig.getMaxReservedAttempts();
    }

    public void setMaxReservedAttempts(int maxReservedAttempts) {
        producerConfig.setMaxReservedAttempts(maxReservedAttempts);
    }

    public long getBaseRetryBackoffMs() {
        return producerConfig.getBaseRetryBackoffMs();
    }

    public void setBaseRetryBackoffMs(long baseRetryBackoffMs) {
        producerConfig.setBaseRetryBackoffMs(baseRetryBackoffMs);
    }

    public long getMaxRetryBackoffMs() {
        return producerConfig.getMaxRetryBackoffMs();
    }

    public void setMaxRetryBackoffMs(long maxRetryBackoffMs) {
        producerConfig.setMaxRetryBackoffMs(maxRetryBackoffMs);
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public void setMdcFields(String mdcFields) {
        this.mdcFields = mdcFields;
    }

    public boolean getIncludeLocation() {
        return this.includeLocation;
    }

    public void setIncludeLocation(boolean includeLocation) {
        this.includeLocation = includeLocation;
    }

    public boolean getIncludeMessage() {
        return this.includeMessage;
    }

    public void setIncludeMessage(boolean includeMessage) {
        this.includeMessage = includeMessage;
    }
}
