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
thread: main
time: 2018-01-02T03:15+0000
__source__: xxx
__topic__: yyy
```
其中：
+ level 日志级别。
+ location 日志打印语句的代码位置。
+ message 日志内容（0.1.11 及以上版本支持通过在 encoder 中设置 pattern 来自定义 message 格式，详见**常见问题**部分）。
+ thread 线程名称。
+ time 日志打印时间。
+ \_\_source\_\_ 日志来源，用户可在配置文件中指定。
+ \_\_topic\_\_ 日志主题，用户可在配置文件中指定。


## 功能优势
+ 日志不落盘：产生数据实时通过网络发给服务端。
+ 无需改造：对已使用logback应用，只需简单配置即可采集。
+ 异步高吞吐：高并发设计，后台异步发送，适合高并发写入。
+ 上下文查询：服务端除了通过关键词检索外，给定日志能够精确还原原始日志文件上下文日志信息。


## 版本支持
* logback 1.2.3
* log-loghub-producer 0.1.13
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
    <version>0.1.11</version>
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
    <accessKey>your accesskey</accessKey>

    <!-- sls 项目配置 -->
    <projectName>your project</projectName>
    <logstore>your logstore</logstore>
    <!--必选项 (end)-->

    <!-- 可选项 -->
    <topic>your topic</topic>
    <source>your source</source>

    <!-- 可选项 详见 '参数说明'-->
    <packageTimeoutInMS>3000</packageTimeoutInMS>
    <logsCountPerPackage>4096</logsCountPerPackage>
    <logsBytesPerPackage>3145728</logsBytesPerPackage>
    <memPoolSizeInByte>104857600</memPoolSizeInByte>
    <retryTimes>3</retryTimes>
    <maxIOThreadSizeInPool>8</maxIOThreadSizeInPool>
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
projectName = [your project]
#日志服务的 logstore 名，必选参数
logstore = [your logstore]
#日志服务的 HTTP 地址，必选参数
endpoint = [your project endpoint]
#用户身份标识，必选参数
accessKeyId = [your accesskey id]
accessKey = [your accesskey]

#被缓存起来的日志的发送超时时间，如果缓存超时，则会被立即发送，单位是毫秒，默认值为3000，最小值为10，可选参数
packageTimeoutInMS = 3000
#每个缓存的日志包中包含日志数量的最大值，不能超过 4096，可选参数
logsCountPerPackage = 4096
#每个缓存的日志包的大小的上限，不能超过 3MB，单位是字节，可选参数
logsBytesPerPackage = 3145728
#Appender 实例可以使用的内存的上限，单位是字节，默认是 100MB，可选参数
memPoolSizeInByte = 1048576000
#指定I/O线程池最大线程数量，主要用于发送数据到日志服务，默认是8，可选参数
maxIOThreadSizeInPool = 8
#指定发送失败时重试的次数，如果超过该值，会把失败信息记录到logback的StatusManager里，默认是3，可选参数
retryTimes = 3

#指定日志主题
topic = [your topic]

#指的日志来源
source = [your source]
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
* 检查您项目中引入的 protobuf-java，aliyun-log-logback-appender 这两个 jar 包的版本是否和文档中`maven 工程中引入依赖`部分列出的 jar 包版本一致。
* 通过观察控制台的输出来诊断您的问题。Aliyun Log Logback Appender 会将 appender 运行过程中产生的异常写入 `ch.qos.logback.core.BasicStatusManager` 中。您可以通过配置 statusListener 来获取 BasicStatusManager 中的数据。例如，`<statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>` 会将 BasicStatusManager 中的数据输出到控制台。
* 请检查您的 `logback.xml` 中是否包含选项 `<shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>`。数据会定期异步地发往服务端，加上此选项可以保证您的程序在正常退出时，内存中缓存的数据不丢失。

## 常见问题

**Q**：是否支持自定义 message 格式？

**A**：0.1.11 及以上版本支持。您可以通过在 encoder 中设置 pattern 来自定义 message 格式，例如：
```
<encoder>
    <pattern>%d %-5level [%thread] %logger{0}: %msg</pattern>
</encoder>
```
message 输出样例：
```
message:  2018-07-15 21:12:29,682 INFO [main] TestAppender: info message.
```

**Q**：日志中为何没有 time 字段？

**A**：0.1.6 以及之前的版本的 LogItem 没有包含 time 字段，请升级至最新版本。

**Q**：用户可以自定义 source 字段的取值吗？

**Q**: 如何采集宿主机 IP？

**A**: 不要在 logback.xml 中设置 source 字段的值，这种情况下 source 字段会被设置成应用程序所在宿主机的 IP。

**A**：0.1.8 以及之前的版本不支持，在这些版本中 source 字段会被设置成应用程序所在宿主机的 IP。在最新的版本中，您可以参考上面的配置文件指定 source 的取值。

**Q**：在网络发生异常的情况下，`aliyun-log-logback-appender` 会如何处理待发送的日志？

**A**：`aliyun-log-logback-appender` 底层使用 `aliyun-log-producer-java` 发送数据。producer 会根据您在配置文件中设置的 `retryTimes` 进行重试，如果超过 `retryTimes` 次数据仍没有发送成功，会将错误信息输出，并丢弃该条日志。关于如何查看错误输出，可以参考错误诊断部分。

**Q**：如何关闭某些类输出的日志？

**A**：通过在 logback.xml 文件中添加 `<logger name="packname" level="OFF"/>` 可屏蔽相应包下日志的输出。
例如，当您在 logback.xml 文件中添加如下内容会屏蔽 package 名为 `com.aliyun.openservices.log.producer.inner` 下所有类的日志输出。
```
<logger name="com.aliyun.openservices.log.producer.inner" level="OFF"/>
```

## 贡献者
[@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy) 对项目作了很大贡献。

感谢 [@lionbule](https://github.com/lionbule) [@zzboy](https://github.com/zzboy) 的杰出工作。
