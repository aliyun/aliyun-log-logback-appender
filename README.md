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
    <accessKeyId>LTAIxdG9ShNoDvYB</accessKeyId>
    <accessKey>6YJUYzi6cLfH3SsL4W20DWkTvUfHJ1</accessKey>

  </appender>
```

