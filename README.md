# Logback Appender

[![Build Status](https://travis-ci.org/aliyun/aliyun-log-logback-appender.svg?branch=master)](https://travis-ci.org/aliyun/aliyun-log-logback-appender)
[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

[中文版README](/README_CN.md)

## Aliyun Log Logback Appender

Logback is intended as a successor to the popular log4j project. You can control the destination of the log through logback. It can be console, file, GUI components, socket, NT event log, syslog. You can control the output format for each log as well. You can control the generation process of the log through log level. The most interesting thing is you can complete the above things through a configuration file and without any code modification.

You can set the destination of your log to AliCloud Log Service through `Aliyun Log Logback Appender`, The format of the log in AliCloud Log Service is as follows:
```
level: ERROR
location: com.aliyun.openservices.log.logback.example.LogbackAppenderExample.main(LogbackAppenderExample.java:18)
message: error log
throwable: java.lang.RuntimeException: xxx
thread: main
time: 2018-01-02T03:15+0000
log: 2018-01-02 11:15:29,682 ERROR [main] com.aliyun.openservices.log.logback.example.LogbackAppenderExample: error log
__source__: xxx
__topic__: yyy
```
Field Specifications:
+ `level` stands for log level
+ `location` is logs's output position
+ `message` is the content of the log
+ `throwable` is exception of the log (this field will appear only if the exception is recorded)
+ `thread` stands for thread name
+ `time` is the log's generation time (you can configure it's format through timeFormat and timeZone)
+ `log` is custom log format(this field will appear only if you configure the encoder)
+ `__source__` is the log's source, you can specify its value in conf file
+ `__topic__` is the log's topic, you can specify its value in conf file

## Advantage
+ `Disk Free`: the generation data will be send to AliCloud Log Service in real time through network.
+ `Without Refactor`: if your application already use logback, you can just add logback appender to your configuration file.
+ `Asynchronous and High Throughput`: the data will be send to AliCloud Log Service asynchronously. It is suitable for high concurrent write.
+ `Context Query`: at server side, in addition to searching log with keywords, you can obtain the context information of original log as well.


## Supported Version
* logback 1.2.3
* log-loghub-producer 0.1.13
* protobuf-java 2.5.0


## Configuration Steps

### 1. Adding the Dependencies in pom.xml

```
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>2.5.0</version>
</dependency>
<dependency>
    <groupId>com.aliyun.openservices</groupId>
    <artifactId>aliyun-log-logback-appender</artifactId>
    <version>0.1.12</version>
</dependency>
```

### 2. Modify the Configuration File

Take `logback.xml` as an example, you can configure the appender and logger related to AliCloud Log Services as follows:
```
  <!-- To prevent data loss when the process exits, please remember to add this configuration -->
  <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>

  <appender name="aliyun" class="com.aliyun.openservices.log.logback.LoghubAppender">
    <!-- Required parameters -->
    <!-- Configure account and network  -->
    <endpoint>your project endpoint</endpoint>
    <accessKeyId>your accesskey id</accessKeyId>
    <accessKey>your accesskey</accessKey>

    <!-- Configure sls -->
    <projectName>your project</projectName>
    <logstore>your logstore</logstore>
    <!-- Required parameters(end) -->

    <!-- Optional parameters -->
    <topic>your topic</topic>
    <source>your source</source>

    <!-- Optional parameters -->
    <packageTimeoutInMS>3000</packageTimeoutInMS>
    <logsCountPerPackage>4096</logsCountPerPackage>
    <logsBytesPerPackage>3145728</logsBytesPerPackage>
    <memPoolSizeInByte>104857600</memPoolSizeInByte>
    <retryTimes>3</retryTimes>
    <maxIOThreadSizeInPool>8</maxIOThreadSizeInPool>
  </appender>

  <!-- This listener will print the status in StatusManager to console
  <statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>
  -->
```
**Note**：
+ To prevent data loss when the process exits, please remember to add label `DelayingShutdownHook`.
+ The LoghubAppender will catch the exceptions in the process of running and put them into `BasicStatusManager`, you can obtain the exception information through `OnConsoleStatusListener` or other means. Reference: https://logback.qos.ch/access.html

## Parameter Description

The `Aliyun Log Logback Appender` provides following parameters.
```
# Specify the project name of your log services, required
projectName = [your project]
# Specify the logstore of your log services, required
logstore = [your logstore]
# Specify the HTTP endpoint of your log services, required
endpoint = [your project endpoint]
# Specify the account information of your log services, required
accessKeyId = [your accesskey id]
accessKey = [your accesskey]

# Specify the timeout for sending package, in milliseconds, default is 3000, the lower bound is 10, optional
packageTimeoutInMS = 3000
# Specify the maximum log count per package, the upper limit is 4096, optional
logsCountPerPackage = 4096
# Specify the maximum cache size per package, the upper limit is 3MB, in bytes, optional
logsBytesPerPackage = 3145728
# The upper limit of the memory that can be used by appender, in bytes, default is 100MB, optional
memPoolSizeInByte = 1048576000
# Specify the I/O thread pool's maximum pool size, the main function of the I/O thread pool is to send data, default is 8, optional
maxIOThreadSizeInPool = 8
# Specify the retry times when failing to send data, if exceeds this value, the appender will record the failure message to BasicStatusManager, default is 3, optional
retryTimes = 3

# Specify the topic of your log
topic = [your topic]

# Specify the source of your log
source = [your source]

# Specify time format of the field time, default is yyyy-MM-dd'T'HH:mmZ, optional
timeFormat = yyyy-MM-dd'T'HH:mmZ

# Specify timezone of the field time, default is UTC, optional
timeZone = UTC
```

## Sample Code

[LogbackAppenderExample.java](/src/main/java/com/aliyun/openservices/log/logback/example/LogbackAppenderExample.java)

[logback-example.xml](/src/main/resources/logback-example.xml)

## Contributors
[@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy) made a great contribution to this project.

Thanks for the excellent work by [@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy).
