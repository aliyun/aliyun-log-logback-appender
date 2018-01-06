# 版本支持
* logback 1.2.3
* log-loghub-producer 0.1.8
* protobuf-java 2.5.0



# 配置步骤

1. **maven 工程中引入依赖**

```
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>2.5.0</version>
</dependency>
<dependency>
    <groupId>com.aliyun.openservices</groupId>
    <artifactId>aliyun-log-logback-appender</artifactId>
    <version>0.1.1</version>
</dependency>
```

2. **修改配置文件**
```
  <!--为了防止进程退出时，内存中的数据丢失，请加上此选项-->
  <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>

  <appender name="aliyun" class="com.aliyun.openservices.log.logback.LoghubAppender">
    <!--必选项-->
    <!-- 账号及网络配置 -->
    <endpoint></endpoint>
    <accessKeyId></accessKeyId>
    <accessKey></accessKey>
    
    <!-- sls 项目配置 -->
    <projectName></projectName>
    <logstore></logstore>
    <!--必选项 (end)-->

    <!-- 可选项 -->
    <topic>xxx</topic>
    
    <!-- 可选项 详见 '参数说明'-->
    <packageTimeoutInMS>3000</packageTimeoutInMS>
    <logsCountPerPackage>4096</logsCountPerPackage>
    <logsBytesPerPackage>3145728</logsBytesPerPackage>
    <memPoolSizeInByte>104857600</memPoolSizeInByte>
    <retryTimes>3</retryTimes>
    <maxIOThreadSizeInPool>8</maxIOThreadSizeInPool>
  </appender>
```

3. **参数说明**
参阅：https://help.aliyun.com/document_detail/43758.html
