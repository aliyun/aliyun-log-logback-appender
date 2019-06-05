# Logback Appender

[![Build Status](https://travis-ci.org/aliyun/aliyun-log-logback-appender.svg?branch=master)](https://travis-ci.org/aliyun/aliyun-log-logback-appender)
[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

[README in English](/README.md)

## Aliyun Log Logback Appender
Logback是由log4j创始人设计的又一个开源日志组件。通过使用Logback，您可以控制日志信息输送的目的地是控制台、文件、GUI 组件、甚至是套接口服务器、NT 的事件记录器、UNIX Syslog 守护进程等；您也可以控制每一条日志的输出格式；通过定义每一条日志信息的级别，您能够更加细致地控制日志的生成过程。最令人感兴趣的就是，这些可以通过一个配置文件来灵活地进行配置，而不需要修改应用的代码。

通过Aliyun Log Logback Appender，您可以控制日志的输出目的地为阿里云日志服务，写到日志服务中的日志的样式如下：
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
其中：
+ level 日志级别。
+ location 日志打印语句的代码位置。
+ message 日志内容。
+ throwable 日志异常信息（只有记录了异常信息，这个字段才会出现）。
+ thread 线程名称。
+ time 日志打印时间（可以通过 timeFormat 或 timeZone 配置 time 字段呈现的格式和时区）。
+ log 自定义日志格式（只有设置了 encoder，这个字段才会出现）。
+ \_\_source\_\_ 日志来源，用户可在配置文件中指定。
+ \_\_topic\_\_ 日志主题，用户可在配置文件中指定。


## 功能优势
+ 日志不落盘：产生数据实时通过网络发给服务端。
+ 无需改造：对已使用logback应用，只需简单配置即可采集。
+ 异步高吞吐：高并发设计，后台异步发送，适合高并发写入。
+ 上下文查询：服务端除了通过关键词检索外，给定日志能够精确还原原始日志文件上下文日志信息。


## 版本支持
* logback 1.2.3
* log-loghub-producer 0.2.0
* protobuf-java 2.5.0


## 配置步骤

### 1. maven 工程中引入依赖

```
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>2.5.0</version>
</dependency>
<dependency>
    <groupId>com.aliyun.openservices</groupId>
    <artifactId>aliyun-log-logback-appender</artifactId>
    <version>0.1.14</version>
</dependency>
```

### 2. 修改配置文件

以xml型配置文件`logback.xml`为例（不存在则在项目根目录创建），配置Loghub相关的appender与 Logger，例如：
```
  <!--为了防止进程退出时，内存中的数据丢失，请加上此选项-->
  <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>

  <appender name="aliyun" class="com.aliyun.openservices.log.logback.LoghubAppender">
    <!--必选项-->
    <!-- 账号及网络配置 -->
    <endpoint>your project endpoint</endpoint>
    <accessKeyId>your accesskey id</accessKeyId>
    <accessKeySecret>your accesskey</accessKeySecret>

    <!-- sls 项目配置 -->
    <project>your project</project>
    <logStore>your logStore</logstore>
    <!--必选项 (end)-->

    <!-- 可选项 -->
    <topic>your topic</topic>
    <source>your source</source>

    <!-- 可选项 详见 '参数说明'-->
    <totalSizeInBytes>104857600</totalSizeInBytes>
    <maxBlockMs>60000</maxBlockMs>
    <ioThreadCount>8</ioThreadCount>
    <batchSizeThresholdInBytes>524288</batchSizeThresholdInBytes>
    <batchCountThreshold>4096</batchCountThreshold>
    <lingerMs>2000</lingerMs>
    <retries>10</retries>
    <baseRetryBackoffMs>100</baseRetryBackoffMs>
    <maxRetryBackoffMs>100</maxRetryBackoffMs>
    
    <!-- 可选项 通过配置 encoder 的 pattern 自定义 log 的格式 -->
    <encoder>
        <pattern>%d %-5level [%thread] %logger{0}: %msg</pattern>
    </encoder>
    
    <!-- 可选项 设置 time 字段呈现的格式 -->
    <timeFormat>yyyy-MM-dd'T'HH:mmZ</timeFormat>
    <!-- 可选项 设置 time 字段呈现的时区 -->
    <timeZone>UTC</timeZone>
  </appender>

  <!-- 可用来获取StatusManager中的状态
  <statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>
  -->
```
**注意**：
+ 为了防止进程退出时，LoghubAppender缓存在内存中的少量数据丢失，请记得加上`DelayingShutdownHook`标签。
+ LoghubAppender在运行过程中产生的异常会被捕获并放入logback的`BasicStatusManager`类中，您可以通过配置`OnConsoleStatusListener`或其他方式查看出错信息。参阅：https://logback.qos.ch/access.html

## 参数说明

Aliyun Log Logback Appender 可供配置的属性（参数）如下，其中注释为必选参数的是必须填写的，可选参数在不填写的情况下，使用默认值。

```
#日志服务的 project 名，必选参数
project = [your project]
#日志服务的 logstore 名，必选参数
logStore = [your logStore]
#日志服务的 HTTP 地址，必选参数
endpoint = [your project endpoint]
#用户身份标识，必选参数
accessKeyId = [your accesskey id]
accessKeySecret = [your accessKeySecret]

#单个 producer 实例能缓存的日志大小上限，默认为 100MB。
totalSizeInBytes=104857600
#如果 producer 可用空间不足，调用者在 send 方法上的最大阻塞时间，默认为 60 秒。
maxBlockMs=60
#执行日志发送任务的线程池大小，默认为可用处理器个数。
ioThreadCount=8
#当一个 ProducerBatch 中缓存的日志大小大于等于 batchSizeThresholdInBytes 时，该 batch 将被发送，默认为 512 KB，最大可设置成 5MB。
batchSizeThresholdInBytes=524288
#当一个 ProducerBatch 中缓存的日志条数大于等于 batchCountThreshold 时，该 batch 将被发送，默认为 4096，最大可设置成 40960。
batchCountThreshold=4096
#一个 ProducerBatch 从创建到可发送的逗留时间，默认为 2 秒，最小可设置成 100 毫秒。
lingerMs=2000
#如果某个 ProducerBatch 首次发送失败，能够对其重试的次数，默认为 10 次。
#如果 retries 小于等于 0，该 ProducerBatch 首次发送失败后将直接进入失败队列。
retries=10
#该参数越大能让您追溯更多的信息，但同时也会消耗更多的内存。
maxReservedAttempts=11
#首次重试的退避时间，默认为 100 毫秒。
#Producer 采样指数退避算法，第 N 次重试的计划等待时间为 baseRetryBackoffMs * 2^(N-1)。
baseRetryBackoffMs=100
#重试的最大退避时间，默认为 50 秒。
maxRetryBackoffMs=100

#指定日志主题，默认为 ""，可选参数
topic = [your topic]

#指的日志来源，默认为应用程序所在宿主机的 IP，可选参数
source = [your source]

#输出到日志服务的时间的格式，默认是 yyyy-MM-dd'T'HH:mmZ，可选参数
timeFormat = yyyy-MM-dd'T'HH:mmZ

#输出到日志服务的时间的时区，默认是 UTC，可选参数（如果希望 time 字段的时区为东八区，可将该值设定为 Asia/Shanghai）
timeZone = UTC
```
参阅：https://github.com/aliyun/aliyun-log-producer-java

## 使用实例
项目中提供了一个名为`com.aliyun.openservices.log.logback.LogbackAppenderExample`的实例，它会加载resources目录下的`logback.xml`文件进行logback配置。

**logback.xml样例说明**
+ 配置了三个appender：loghubAppender1、loghubAppender2、STDOUT。
+ loghubAppender1：将日志输出到project=test-proj，logstore=store1。输出WARN、ERROR级别的日志。
+ loghubAppender2：将日志输出到project=test-proj，logstore=store2。只输出INFO级别的日志。
+ STDOUT：将日志输出到控制台。由于没有对日志级别进行过滤，会输出root中配置的日志级及以上的所有日志。

[LogbackAppenderExample.java](/src/main/java/com/aliyun/openservices/log/logback/example/LogbackAppenderExample.java)

[logback-example.xml](/src/main/resources/logback-example.xml)

## 错误诊断

如果您发现数据没有写入日志服务，可通过如下步骤进行错误诊断。
1. 检查配置文件 logback.xml 是否限定了 appender 只输出特定级别的日志。比如，是否设置了 root 或 logger 的 level 属性，是否在 appender 中设置了 [filter](https://logback.qos.ch/manual/filters.html)。
2. 检查您项目中引入的 protobuf-java，aliyun-log-logback-appender 这两个 jar 包的版本是否和文档中`maven 工程中引入依赖`部分列出的 jar 包版本一致。
3. 通过观察控制台的输出来诊断您的问题。Aliyun Log Logback Appender 会将 appender 运行过程中产生的异常写入 `ch.qos.logback.core.BasicStatusManager` 中。您可以通过配置 statusListener 来获取 BasicStatusManager 中的数据。例如，`<statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>` 会将 BasicStatusManager 中的数据输出到控制台。
4. 请检查您的 `logback.xml` 中是否包含选项 `<shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>`。数据会定期异步地发往服务端，加上此选项可以保证您的程序在正常退出时，内存中缓存的数据不丢失。

## 常见问题

**Q**：是否支持自定义 log 格式？

**A**：在 0.1.12 及以上版本新增了 log 字段。您可以通过在 encoder 中设置 pattern 来自定义 log 格式，例如：
```
<encoder>
    <pattern>%d %-5level [%thread] %logger{0}: %msg</pattern>
</encoder>
```
log 输出样例：
```
log:  2018-07-15 21:12:29,682 INFO [main] TestAppender: info message.
```

**Q**：日志中为何没有 time 字段？

**A**：0.1.6 以及之前的版本的 LogItem 没有包含 time 字段，请升级至最新版本。

**Q**：用户可以自定义 source 字段的取值吗？

**A**：0.1.8 以及之前的版本不支持，在这些版本中 source 字段会被设置成应用程序所在宿主机的 IP。在最新的版本中，您可以参考上面的配置文件指定 source 的取值。

**Q**: 如何采集宿主机 IP？

**A**: 不要在 logback.xml 中设置 source 字段的值，这种情况下 source 字段会被设置成应用程序所在宿主机的 IP。

**Q**：在网络发生异常的情况下，`aliyun-log-logback-appender` 会如何处理待发送的日志？

**A**：`aliyun-log-logback-appender` 底层使用 `aliyun-log-producer-java` 发送数据。producer 会根据您在配置文件中设置的 `retryTimes` 进行重试，如果超过 `retryTimes` 次数据仍没有发送成功，会将错误信息输出，并丢弃该条日志。关于如何查看错误输出，可以参考错误诊断部分。

**Q**：如何关闭某些类输出的日志？

**A**：通过在 logback.xml 文件中添加 `<logger name="packname" level="OFF"/>` 可屏蔽相应包下日志的输出。
例如，当您在 logback.xml 文件中添加如下内容会屏蔽 package 名为 `com.aliyun.openservices.log.producer.inner` 下所有类的日志输出。
```
<logger name="com.aliyun.openservices.log.producer.inner" level="OFF"/>
```

**Q**：应用初始化时出现这样的信息 `A number (N) of logging calls during the initialization phase have been intercepted and are now being replayed. These are subject to the filtering rules of the underlying logging system.`？

**A**：该信息只会在日志系统初始化阶段产生，并不影响后续日志记录的功能。

当应用首次调用`LoggerFactory.getLogger()`方法时，日志系统进入初始化流程。初始化流程还未结束，再次调用`LoggerFactory.getLogger()`方法便会出现上述信息。这时，slf4j 会创建替代记录器（substitute loggers）并返回。在完成初始化后，替代记录器（substitute loggers）会将日志记录请求委托给合适的 logger。

`aliyun-log-logback-appender` 的依赖库 `aliyun-log-producer-java` 也会使用 slf4j 记录日志，所以会出现上述信息。

参阅：https://www.slf4j.org/codes.html#replay

**Q**：如果想设置 `time` 字段的时区为东八区或其他时区，该如何指定 `timeZone` 的取值？

**A**：当您将 `timeZone` 指定为 `Asia/Shanghai` 时，`time` 字段的时区将为东八区。timeZone 字段可能的取值请参考 [java-util-timezone](http://tutorials.jenkov.com/java-date-time/java-util-timezone.html)。

## 贡献者
[@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy) 对项目作了很大贡献。

感谢 [@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy) 的杰出工作。
