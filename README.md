# aliyun-log-logback-appender

# 版本支持
* logback 1.2.3
* log-loghub-producer 0.1.8
* protobuf-java 2.5.0

# logback appender config
```
  <appender name="aliyun" class="com.aliyun.openservices.LoghubAppender">
    <!--必选项-->
    <!-- 账号及网络配置 -->
    <endpoint></endpoint>
    <accessKeyId></accessKeyId>
    <accessKey></accessKey>
    
    <!-- sls 项目配置 -->
    <projectName></projectName>
    <logstore></logstore>
    <!--必选项 (end)-->
    
    <!-- 可选项 详见 '参数说明'-->
    <packageTimeoutInMS>3000</packageTimeoutInMS>
    <logsCountPerPackage>4096</logsCountPerPackage>
    <logsBytesPerPackage>3145728</logsBytesPerPackage>
    <memPoolSizeInByte>104857600</memPoolSizeInByte>
    <retryTimes>3</retryTimes>
    <maxIOThreadSizeInPool>8</maxIOThreadSizeInPool>
    <shardHashUpdateIntervalInMS>600000</shardHashUpdateIntervalInMS>    
  </appender>
```

# 单元测试
  详见 class TestAppender
  为了保证把消息发出去，必须延长线程的生命周期。具体代码如下：
```
    try {
        ProducerConfig config = new ProducerConfig();
        Thread.sleep(2 * config.packageTimeoutInMS);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
```

# 参数说明
* endpoint    网络相关(待补充)
* accessKeyId 账号相关(待补充)
* accessKey   账号相关(待补充)
* projectName sls配置相关(待补充)
* logstore    sls配置相关(待补充)
* packageTimeoutInMS	指定被缓存日志的发送超时时间，如果缓存超时，则会被立即发送。	整数形式，单位为毫秒。
* logsCountPerPackage	指定每个缓存的日志包中包含日志数量的最大值。	整数形式，取值为1~4096。
* logsBytesPerPackage	指定每个缓存的日志包的大小上限。	整数形式，取值为1~5242880，单位为字节。
* memPoolSizeInByte	指定单个Producer实例可以使用的内存的上限。	整数形式，单位为字节。
* maxIOThreadSizeInPool	指定I/O线程池最大线程数量，主要用于发送数据到日志服务。	整数形式。
* shardHashUpdateIntervalInMS	指定更新Shard的Hash区间的时间间隔，当指定shardhash的方式发送日志时，需要设置此参数。
后端merge线程会将映射到同一个Shard的数据merge在一起，而Shard关联的是一个Hash区间，Producer在处理时会将用户传入的Hash映射成Shard关联Hash区间的最小值。每一个Shard关联的Hash区间，Producer会定时从LogHub拉取。	整数形式。
* retryTimes	指定发送失败时重试的次数，如果超过该值，就会将异常作为callback的参数，交由用户处理。	整数形式。

参阅：
https://help.aliyun.com/document_detail/43758.html

# 设计思路
### 设计目标
基于slf4j的logger接口，通过网络采集log，在服务端进行log的管理和分析。

### 为啥继承UnsynchronizedAppenderBase接口？
Appender是log处理的输出接口。UnsynchronizedAppenderBase是多线程非同步的方式对log内容进行处理的基类。
网络输出log无时序约束，多线程采集才是最高效的。另外输出的信息无layout的约束，因此UnsynchronizedAppenderBase是最佳父类。

### 重要接口说明
public void start()
负责系统参数的初始化

public void stop()
退出时，负责清空缓存中的log内容，另外关闭网络服务的连接
