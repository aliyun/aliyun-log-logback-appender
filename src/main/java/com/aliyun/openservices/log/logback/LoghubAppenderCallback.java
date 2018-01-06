package com.aliyun.openservices.log.logback;

import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.producer.ILogCallback;
import com.aliyun.openservices.log.response.PutLogsResponse;

import java.util.List;

/**
 * Created by brucewu on 2018/1/5.
 */
public class LoghubAppenderCallback<E> extends ILogCallback {

    private LoghubAppender<E> loghubAppender;

    private String project;

    private String logstore;

    private String topic;

    private String source;

    private List<LogItem> logItems;

    public LoghubAppenderCallback(LoghubAppender<E> loghubAppender, String project, String logstore, String topic,
                                  String source, List<LogItem> logItems) {
        super();
        this.loghubAppender = loghubAppender;
        this.project = project;
        this.logstore = logstore;
        this.topic = topic;
        this.source = source;
        this.logItems = logItems;
    }

    public void onCompletion(PutLogsResponse putLogsResponse, LogException e) {
        if (e != null) {
            loghubAppender.addError("Failed to putLogs. project=" + project + " logstore=" + logstore +
                    " topic=" + topic + " source=" + source + " logItems=" + logItems, e);
        }
    }
}
