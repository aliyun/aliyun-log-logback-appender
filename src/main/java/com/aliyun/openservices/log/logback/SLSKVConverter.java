package com.aliyun.openservices.log.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: aliyun-log-logback-appender
 * @description: used to add user-define key-value to the sls.
 * @author: tommy.tb
 * @create: 2019-01-18 20:10
 **/
public abstract class SLSKVConverter extends ClassicConverter {

    /**
     * Implement this method to add your interested kvs.
     * @param kvs
     */
    public abstract void addKeyValue(Map<String, String> kvs, ILoggingEvent iLoggingEvent);

    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        Map<String, String> kvMap = new HashMap<String, String>();
        addKeyValue(kvMap, iLoggingEvent);
        StringBuilder sb = new StringBuilder();
        sb.append("SLS_KV#");
        int size = kvMap.size();
        int p = 1;
        for(Map.Entry<String,String> item : kvMap.entrySet()){
            sb.append(item.getKey()).append(":").append(item.getValue());
            if(p ++ < size){
                sb.append("|");
            }
        }
        sb.append("#");
        return sb.toString();
    }
}
