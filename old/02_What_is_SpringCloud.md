## What is Spring Cloud

官网：https://spring.io/projects/spring-cloud

版本：Greenwich SR3

出自官方：

```bash
OVERVIEW
Spring Cloud provides tools for developers to quickly build some of the common patterns in distributed systems(e.g. configuration management, service discovery, circuit breakers, intelligent routing, micro-proxy, controls bus, one-time tokens, global locks, leadership election, distributed sessions, cluster state).
Coordination of distributed systems leads to boiler plate patterns, and using Spring Cloud developers can quickly stand up services and applications that implement those patterns.
They will work well in any distributed environment, including the developer's own laptop, bare metal data centers, and managed platforms such as Cloud Foundry.

翻译：
Spring Cloud为开发人员提供了一些工具用来快速构建分布式系统中一些常见模式和解决一些常见问题（例如配置管理、服务发现、断路器、智能路由、微代理、控制总线、一次性令牌、全局锁、领导选举、分布式会话、集群状态）。
分布式系统的协调导致了很多样板式的代码（很多固定套路的代码），使用Spring Cloud开发人员可以快速建立实现这些模式的服务和应用程序。
它们在任何分布式环境中都能很好地运行，包括开发人员自己的笔记本电脑、裸机数据中心和云计算等托管平台。

（微服务也属于分布式，分布式也是微服务，它们你中有我我中有你，但是它们所追求的目标有区别；
distributed systems分布式系统，Spring Cloud解决了分布式系统开发中的一系列问题，然后我们使用这样一套方案，Spring Cloud可以称作一套组件，一套方案，它里面是由很多组件构成的。它就可以帮我们解决一些问题，开发时遇到的分布式问题例如领导选举、分布式会话等等都将有一套方案，倒是直接用即可。）
```

微服务有很多个问题需要进行解决，Spring Cloud下由多个子项目进行构成。

目前看到Spring Cloud下的组件有：

* Spring Cloud
  * Spring Cloud Azure
  * Spring Cloud Alibaba
  * Spring Cloud for Amazon Web Services
  * Spring Cloud Bus
  * Spring Cloud Circuit Breaker
  * Spring Cloud CLI
  * Spring Cloud for Cloud Foundry
  * Spring Cloud - Cloud Foundry Service Broker
  * Spring Cloud Cluster
  * Spring Cloud Commons
  * Spring Cloud Config
  * Spring Cloud Connectors
  * Spring Cloud Consul
  * Spring Cloud Contract
  * Spring Cloud Function
  * Spring Cloud Gateway
  * Spring Cloud GCP
  * Spring Cloud Kubernetes
  * Spring Cloud Netflix
  * Spring Cloud Open Service Broker
  * Spring Cloud OpenFeign
  * Spring Cloud Pipelines
  * Spring Cloud Schema Registry
  * Spring Cloud Security
  * Spring Cloud Skipper
  * Spring Cloud Sleuth
  * Spring Cloud Stream
  * Spring Cloud Stream App Starters
  * Spring Cloud Stream Applications
  * Spring Cloud Task
  * Spring Cloud Task App Starters
  * Spring Cloud Vault
  * Spring Cloud Zookeeper
  * Spring Cloud App Broker

当前稳定版本Greenwich SR3 `CURRENT` `GA`

![image-20210511225849002](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210511225849002.png)

在国内，版本之间的称呼：

Greenwich SR3称之为G版；

Hoxton RC1称之为H版；

Finchley SR4称之为F版；

或者还有A版、B版、D版；

它是从A~Z这个顺序去进行发布的版本，看首字母。

下一个版本的字母会比上一个版本的字母大。

当下去看版本：

![image-20210511230003975](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210511230003975.png)



### Spring Cloud特性

Spring Cloud为分布式系统开发的 典型应用场景 提供良好的 开箱即用的功能。

比如：

分布式/版本化配置

服务注册和发现

路由

服务与服务间的调用

负载均衡

断路器

全局锁

领导选举与集群状态

分布式消息传递

由于要解决不止是以上的问题，Spring Cloud下提供了很多的组件来进行解决相关问题。

### Spring Cloud下的主要项目

* Spring Cloud Config
* Spring Cloud Netflix
* Spring Cloud Bus
* Spring Cloud for Cloud Foundry
* Spring Cloud Open Service Broker
* Spring Cloud Cluster
* Spring Cloud Consul
* Spring Cloud Security
* Spring Cloud Sleuth
* Spring Cloud Data Flow
* Spring Cloud Stream
* Spring Cloud Stream App Starters
* Spring Cloud Task
* Spring Cloud Task App Starters
* Spring Cloud Zookeeper
* Spring Cloud AWS
* Spring Cloud Connectors
* Spring Cloud Starters
* Spring Cloud CLI
* Spring Cloud Contract
* Spring Cloud Gateway
* Spring Cloud OpenFeign
* Spring Cloud Pipelines
* Spring Cloud Function
* ...



### Spring Cloud 的版本

Spring Cloud是由一系列独立项目组成的，每个独立的项目具有不同的发布节奏。

每次Spring Cloud发布版本时，就会组合这一系列的子项目，Spring Cloud为了避免大家对版本号的误解，避免与子项目版本号混淆，所以Spring Cloud发布的版本是一个按照字母顺序的伦敦地铁站的名字，

（“天使”是第一个版本，“布里克斯顿”是第二个）字母顺序是从A~Z，目前最新稳定版本Greenwich SR3，

当Spring Cloud里面的某些子项目出现关键性bug或重大更新，则发布序列将推出名称以".SRX"结尾的版本，其中“X”是一个数字，比如：Greenwich SR1、Greenwich SR2、Greenwich SR3；

因为Spring Cloud当中有多个子项目，比如说Spring Cloud Pipelines该子项目当中有一个重大的bug，在进行修改了之后进行发布了新版本，那么Spring Cloud是使用到了这个子项目的，该子项目发布了新版本，Spring Cloud的版本也是需要进行跟进的，如果不进行跟进的话，那么到时候Spring Cloud依赖Spring Cloud Pipelines该子项目的是之前的那个旧的版本，即有重大bug的版本，所以就不得不将Spring Cloud版本进行升级一下，那么在升级的时候，不想变化总版本，即变化字母的大版本，那么这个时候".SRX"的重要性就凸显出来了。

目前看到Greenwich SR3就说明有过三次的重大bug修复或者是更新，但是其总版本还是G版本不变，即G版本中依赖的某一个组件模块发生了重大的调整更新，所以就是用".SR"加“X”数值来进行实现版本的升级。



Spring Cloud是微服务开发的一整套解决方案，采用Spring Cloud开发，每个项目依然是使用Spring Boot；

所以在学习Spring Cloud之前先要进行学习Spring Boot；

由于底层是使用的Spring Boot，那么在使用时需要注意Spring Cloud和Spring Boot的兼容版本；

Spring Cloud与Spring Boot的兼容版本表格

| Spring Cloud版本 | Spring Boot版本 |
| ---------------- | --------------- |
| Hoxton           | 2.2.x           |
| Greenwich        | 2.1.x           |
| Finchley         | 2.0.x           |
| Edgware          | 1.5.x           |
| Dalston          | 1.5.x           |
| Camden           | 1.4.x 或 1.5.x  |
| Brixton          | 1.3.x 或 1.4.x  |
| Angel            | 1.2.x           |

Greenwich简称G版本需要使用Spring Boot的2.1.x版本，如果使用了Spring Boot的2.2.x版本的话，并不能保证不会报错或者启动运行这样类似的问题。

由于G版本 Greenwich只在2.1.x中做了严格测试是没有问题的。

G版本搭配Spring Boot除了2.1.x版本使用其他的版本是没有测试过的。所以最好是用测试过匹配的这个版本进行开发项目。

2.1.x即可以是2.1.0、2.1.1等等，2.1.x系列即可；

`GA`稳定版本；`PRE`预发布版本