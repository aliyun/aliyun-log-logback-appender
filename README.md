# aliyun-log-logback-appender

# 版本支持
* logback 1.2.3
* log-loghub-producer 0.1.8
* protobuf-java 2.5.0

# logback config
```
  <appender name="aliyun" class="com.aliyun.openservices.LoghubAppender">
    <!--必选项-->
    <projectName>ali-sts-token-test</projectName>
    <logstore>test</logstore>
    <endpoint>cn-hangzhou.log.aliyuncs.com</endpoint>
    <accessKeyId></accessKeyId>
    <accessKey></accessKey>

  </appender>
```

