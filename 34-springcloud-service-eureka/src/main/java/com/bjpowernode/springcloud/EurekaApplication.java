package com.bjpowernode.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer //开启Eureka服务端
@SpringBootApplication
public class EurekaApplication {
/*
* 在 Eureka server项目的main方法上 添加@EnableEurekaServer注解，用于开启Eureka注册中心服务端
* Enable ： 让 ...something 开启，让 ....something 启用
* 添加好注解 @EnableEurekaServer注解之后，接着在 application.properties 文件当中配置 Eureka服务注册中心 相关信息
* */

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }

    /*
    所以eureka-server也是一个springBoot项目
    该项目当中配置非常简单，
    第一步，加 spring-cloud-starter-netflix-eureka-server 的依赖
    第二步，进行配置 application.properties 当中的eureka相关内容
    第三部，写代码，即在eureka-server 服务中的main方法中添加 @EnableEurekaServer 开启Eureka 服务注册中心
    它还是和原来的spring boot 开发差不多类似
    其他什么都不用做即可
    在 eureka-server 该服务启动好之后，该eureka-server是没有进行配置项目工程路径的
    因为eureka-server就是tomcat启动一个web项目，那么访问的时候直接是 http://127.0.0.1:8761 即可 或者 http://localhost:8761 即可
    没有项目工程路径，那么就是直接 ip:端口直接访问注册中心即可

    Tomcat started on port(s): 8761 (http) with context path ''
2021-05-19 21:29:55.030  INFO 992 --- [           main] .s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8761
2021-05-19 21:29:55.690  INFO 992 --- [           main] c.b.springcloud.EurekaApplication        : Started EurekaApplication in 7.227 seconds (JVM running for 9.367)
2021-05-19 21:29:55.986  INFO 992 --- [on(4)-127.0.0.1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2021-05-19 21:29:55.986  INFO 992 --- [on(4)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2021-05-19 21:29:55.995  INFO 992 --- [on(4)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 9 ms
    控制台看到上述，即表示启动成功


    浏览器地址栏打开 http://localhost:8761 之后
    看到的即 eureka-server的首页
    System Status 系统的状态
    Environment test 测试环境
    Data center default  数据中心为default 默认
    Current time 当前时间
    Uptime 运行时间
    Lease expiration enabled false 最近的过期的
    Renews threshold 1  是否重新续约 阈值为1
    Renews(last min) 0 重新续约 重新刷新 最近的一分钟内有没有做过重新的续约

    DS Replicas 数据源的复制 副本 DS DataSource
    Instances currently registered with Eureka 当前注册的实例，即当前有多少个服务注册到自身 注册中心上了
    目前注册中心上什么都没有，一个服务都没有 No instances available；因为还没有进行注册，所以它是没有的，是空的
    Application       AMIs             Availability Zones     Status
    No instances available 目前没有数据源进行复制（到时候搭建集群的时候就会有了）

    General Info 产生的信息
    Name            Value
    total-avail-memory 内存信息      472mb
    environment 环境信息     test
    num-of-cpus  cpu的个数    8
    current-memory-usage 当前内存使用了多少   143mb(30%)
    server-uptime 服务运行了多长时间   00:01
    registered-replicas  注册的副本
    unavailable-replicas 不可用的副本
    available-replicas 可用的副本

    Instance Info 实例的信息
    Name             Value
    ipAddr  ip地址    192.168.0.104
    status  状态        UP  UP表示开着的

    以上是首页 HOME
    后面还有 LAST 1000 SINCE STARTUP 最近的一些信息
    System Status和上面的HOME中一致
    下面的DS Replicas 就不一样了
    DS Replicas 数据源副本
    Last 1000 cancelled leases 最近1000个取消注册的信息 |   Last 1000 newly registered leases 最近1000次注册的信息（到时候就将在下面列出来，现在一个都没有所以是No Result available）
    Timestamp       Lease
    No results available

    EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.
紧急情况！EUREKA可能错误地声称实例在没有启动的情况下启动了。续订小于阈值，因此实例不会为了安全而过期


    注册中心启动成功之后，接下来看一下；开始向注册中心 进行 注册服务
    3-7. 向 Eureka服务注册中心注册服务
    我们前面搭建了 服务提供者项目，接下来我们就可以将该 服务提供者注册到 Eureka 服务注册中心，步骤如下：
    1. 在该 服务提供者中 添加 eureka的依赖，因为服务提供者 向注册中心 注册服务，需要连接 eureka，所以需要 eureka 客户端的支持
    <!--spring-cloud-starter-netflix-eureka-client-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    2. 激活 Eureka 中的 EnableEurekaClient 功能：在SpringBoot的入口函数处，通过添加 @EnableEurekaClient 注解来表明自己是一个 eureka客户端，
    让我的服务提供者 可以连接 eureka 注册中心；

    要往eureka-server上进行注册服务，eureka是 客户端-服务端模式，那么http://localhost:8761 即服务端，准备好了
    而客户端即我们的程序，goods服务和portal服务都是eureka客户端，goods服务是服务提供者，portal是服务消费者，它们都是eureka client 客户端
    那么此时goods和portal服务都可以往 eureka-server 注册中心 服务进行注册
    首先看服务提供者如何进行注册
    */
}
