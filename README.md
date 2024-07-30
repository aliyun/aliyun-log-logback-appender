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
+ `location` is logs's output position, can be disabled by configuration
+ `message` is the content of the log
+ `throwable` is exception of the log (this field will appear only if the exception is recorded)
+ `thread` stands for thread name
+ `time` is the log's generation time (you can configure it's format through timeFormat and timeZone)
+ `log` is custom log format (this field will appear only if you configure the encoder)
+ `__source__` is the log's source, you can specify its value in conf file
+ `__topic__` is the log's topic, you can specify its value in conf file

## Advantage
+ `Disk Free`: the generation data will be send to AliCloud Log Service in real time through network.
+ `Without Refactor`: if your application already use logback, you can just add logback appender to your configuration file.
+ `Asynchronous and High Throughput`: the data will be send to AliCloud Log Service asynchronously. It is suitable for high concurrent write.
+ `Context Query`: at server side, in addition to searching log with keywords, you can obtain the context information of original log as well.


## Supported Version
* logback 1.2.3
* aliyun-log-producer 0.3.8
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
    <version>0.1.25</version>
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
    <accessKeySecret>your accesskey</accessKeySecret>

    <!-- Configure sls -->
    <project>your project</project>
    <logStore>your logStore</logStore>
    <!-- Required parameters(end) -->

    <!-- Optional parameters -->
    <topic>your topic</topic>
    <source>your source</source>

    <!-- Optional parameters -->
    <totalSizeInBytes>104857600</totalSizeInBytes>
    <maxBlockMs>0</maxBlockMs>
    <ioThreadCount>8</ioThreadCount>
    <batchSizeThresholdInBytes>524288</batchSizeThresholdInBytes>
    <batchCountThreshold>4096</batchCountThreshold>
    <lingerMs>2000</lingerMs>
    <retries>10</retries>
    <baseRetryBackoffMs>100</baseRetryBackoffMs>
    <maxRetryBackoffMs>50000</maxRetryBackoffMs>
    
    <!-- Optional parameters -->
    <encoder>
        <pattern>%d %-5level [%thread] %logger{0}: %msg</pattern>
    </encoder>
    
    <!--  Optional parameters -->
    <timeFormat>yyyy-MM-dd'T'HH:mmZ</timeFormat>
    <!--  Optional parameters -->
    <timeZone>UTC</timeZone>
    <!--  Optional parameters -->
    <includeLocation>true</includeLocation>
    <!--  Optional parameters -->
    <includeMessage>true</includeMessage>
    <!-- Optional parameters -->
    <!-- See chapter "Custom CredentialsProvider" -->
    <credentialsProviderBuilder class="com.aliyun.openservices.log.logback.example.ExampleCredentialsProviderBuilder">
            <accessKeyId>${accessKeyId}</accessKeyId>
            <accessKeySecret>${accessKeySecret}</accessKeySecret>
    </credentialsProviderBuilder>
    <!-- optional, option for maximum exception stack trace recording length. Exceeding this length will result in truncation. The default value is 500,
      maximum is 5000000, -1 will be regarded as 5000000 -->
    <maxThrowable>500</maxThrowable>
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
project = [your project]
# Specify the logstore of your log services, required
logStore = [your logStore]
# Specify the HTTP endpoint of your log services, required
endpoint = [your project endpoint]
# Specify the account information of your log services, required
accessKeyId = [your accesskey id]
accessKeySecret = [your accessKeySecret]

# The upper limit log size that a single producer instance can hold, default is 100MB.
totalSizeInBytes=104857600
# If the producer has insufficient free space, the caller's maximum blocking time on the send method, defaults is 60 seconds. In order not to block the log printing thread, it is strongly recommended to set this value to 0.
maxBlockMs=0
# The thread pool size for executing log sending tasks, defaults is the number of processors available.
ioThreadCount=8
# When the size of the cached log in a Producer Batch is greater than or equal batchSizeThresholdInBytes, the batch will be send, default is 512KB, maximum can be set to 5MB.
batchSizeThresholdInBytes=524288
# When the number of log entries cached in a ProducerBatch is greater than or equal to batchCountThreshold, the batch will be send.
batchCountThreshold=4096
# A ProducerBatch has a residence time from creation to sending, defaulting is 2 seconds and a minimum of 100 milliseconds.
lingerMs=2000
# The number of times a Producer Batch can be retried if it fails to send for the first time, default is 10.
retries=10
# The backoff time for the first retry, default 100 milliseconds.
baseRetryBackoffMs=100
# The maximum backoff time for retries, default is 50 seconds.
maxRetryBackoffMs=50000

# Specify the topic of your log, default is "", optional
topic = [your topic]

# Specify the source of your log, default is host ip, optional
source = [your source]

# Specify time format of the field time, default is yyyy-MM-dd'T'HH:mmZ, optional
timeFormat = yyyy-MM-dd'T'HH:mmZ

# Specify timezone of the field time, default is UTC, optional
timeZone = UTC
# Whether to record the Location field, defaults to true, set it false if performance-sensitive
includeLocation = true
# Whether to include field message when encoder exists, defaults to true.
includeMessage = true
# option for maximum exception stack trace recording length. Exceeding this length will result in truncation. Defaults to 500, optional. maximum is 5000000, -1 will be regarded as 5000000.
maxThrowable=500
```

## Custom CredentialsProvider
The logback-appender supports custom `CredentialsProvider`. By implementing the `CredentialsProvider` interface, you can implement advanced features yourself, such as dynamic rotation of AccessKey.  

> Once a `CredentialsProvider` is provided, it's unnecessary to configure parameter for static credentials (AccessKeyId/AccessKeySecret/SecurityToken); the system will use the credentials fetched through the `CredentialsProvider` provided.  

 1. Defines a class, eg. `MyCredentialsProvider`, and implements the `CredentialsProvider` interface to support the dynamic updates of credentials. Make sure the implementation is thread-safe.
    - For performance, it is recommended that method `getCredentials` caches credentials and refreshes them before expired. 
 
    ```java
    class MyCredentialsProvider implements CredentialsProvider {
        @Override
        public synchronized Credentials getCredentials() {
            // fetch credentials and caches credentials
        }
        // constructor
        MyCredentialsProvider(String param1, long paramField2) {}
    }
    ```

 2. Defines a class, eg. `MyBuilder`, and implements the `CredentialsProviderBuilder` interface. 
    - The method `getCredentialsProvider`  should return a new instance of `CredentialsProvider` for each call.
    ```java
    class MyBuilder implements CredentialsProviderBuilder {
      @Override
      public CredentialsProvider getCredentialsProvider() {
         return new MyCredentialsProvider(param1, paramField2);
      }
      private String param1;
      private long paramField2;
      public void setParam1(String param1) {
          this.param1 = param1;
      }
      public void setParamField2(long paramField2) {
          this.paramField2 = paramField2;
      }
    }
    ```
    
3. Configure logback with xml file, set the class of `credentialsProviderBuilder` with the fully qualified name of your class, eg. `com.example.MyBuilder`, and pass any custom parameters.
    ```xml
      <appender name="aliyun" class="com.aliyun.openservices.log.logback.LoghubAppender">
        <credentialsProviderBuilder class="com.example.MyBuilder">
            <param1>hello</param1>
            <paramField2>123</paramField2>
        </credentialsProviderBuilder> 
        <!-- Omit other configs -->
      </appender>
    ```
### Custom parameter
To pass in some custom parameters to the `MyBuilder` class, such as param1, paramField2, define the setter methods setParam1 and setParamField2 in the class first.
```java
class MyBuilder implements CredentialsProviderBuilder {
    private String param1;
    private long paramField2;
    public void setParam1(String param1) {
        this.param1 = param1;
    }
    public void setParamField2(long paramField2) {
        this.paramField2 = paramField2;
    }
    // Omit other codes
}
```
Then configure logback with xml file, set the class of `credentialsProviderBuilder` with the fully qualified name of your class, eg. `com.example.MyBuilder`, and fill with custom parameters.
```xml
<appender name="aliyun" class="com.aliyun.openservices.log.logback.LoghubAppender">
    <credentialsProviderBuilder class="com.example.MyBuilder">
        <param1>hello</param1>
        <paramField2>123</paramField2>
    </credentialsProviderBuilder>
<!--  Omit other configs -->
</appender>
```

## Sample Code

[LogbackAppenderExample.java](/src/main/java/com/aliyun/openservices/log/logback/example/LogbackAppenderExample.java)

[logback-example.xml](/src/main/resources/logback-example.xml)

## Contributors
[@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy) made a great contribution to this project.

Thanks for the excellent work by [@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy).

