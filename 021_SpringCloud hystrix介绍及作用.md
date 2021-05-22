## 021_SpringCloud hystrix介绍及作用

微服务专题下的spring cloud，一站式微服务架构解决方案。

### 1、快速回顾

首先对分布式与微服务架构的理论进行梳理，系统进化的过程，从单体应用到分布式微服务。

接着介绍什么是Spring cloud？Spring Cloud是解决微服务问题的一系列解决方案。从2014年出现了Spring Cloud。

Spring Cloud的整体架构与dubbo非常相似。可以和Dubbo做一个比较。Spring Cloud也有服务提供者也有服务消费者，也有注册中心，整体结构非常相似。

服务消费者是controller，服务提供者也是controller，两者即是controller调用controller。底层的开发是使用Springboot进行开发的。controller调用controller。

然后进行学习了spring cloud当中的注册中心叫做eureka，eureka注册中心。在dubbo当中大部分情况下是使用zookeeper作为注册中心。

spring cloud当中的eureka和zookeeper它们之间的侧重点不一样，zookeeper是一致性（数据一致性），eureka是可用性。两者角度不一样，eureka保证可用性A，zookeeper保证数据一致性C，它们都需要保证分区容错性P。

接着使用Spring Cloud Eureka进行搭建高可用注册中心集群。即该注册中心存在有三个节点，搭建了一个集群，避免注册中心出现宕机问题，如果在线上服务，那肯定是有好几个节点的。

spring cloud eureka当中有一个自我保护机制，所以在浏览器页面访问eureka 注册中心首页的时候时，有个时候会出现红色的字体，如果出现了红色即触发了eureka的自我保护机制。

紧接着上节课讲解了spring cloud ribbon 负载均衡。以及spring cloud feign 声明式的服务调用。一个是ribbon，一个是feign。

在spring cloud当中其实就是由各种各样的组件构成的，而且这些组件都是由Netflix公司下的开源组件，如Eureka、Ribbon、Feign都是Netflix公司下的开源组件，Netflix公司提供的。

Netflix 国外一家做视频版权服务的公司。它们公司开源了很多的项目，它们公司叫做Netflix。

下面需要新学习一个组件，这个组件依然是Netflix公司提供的 Hystrix，Spring Cloud Hystrix。这是spring cloud当中的另外一个组件。

因为spring cloud即通过一个个的组件进行构成的。spring cloud是用来解决分布式微服务开发时候的一系列问题，比如说eureka解决注册中心问题，ribbon解决负载均衡调用时候的问题，feign即解决了声明式调用服务问题。即它们这些组件都在解决问题。

接下来的spring cloud Hystrix也是解决微服务开发当中的一些问题。

1. 分布式与微服务架构的理论梳理；
2. 什么是Spring Cloud？
3. Spring Cloud的整体架构
4. 服务消费者controller直连调用服务提供者controller
5. spring cloud的注册中心Eureka
6. Spring Cloud Eureka与Zookeeper比较
7. Spring Cloud Eureka高可用集群
8. Spring Cloud Eureka自我保护机制
9. Spring Cloud Ribbon 负载均衡
10. Spring Cloud Feign 声明式服务调用

### 2、Spring Cloud Hystrix

#### Hystrix是什么

首先来认识一下 Hystrix 是什么东西？

Hystrix 被称为 熔断器（或者叫做 断路器，对于Hystrix可能有一些名词说法不一样，熔断器、断路器说的都是Hystrix），它是一个用于处理 分布式系统的延迟和容错的开源库（也就是说Hystrix是一个jar包，开源的库当中提供的jar包），

在分布式（或者微服务）系统里，许多服务之间通过 远程调用 实现信息交互，调用时不可避免会出现 调用失败，比如（调用）超时、（调用）异常（或者是服务不可用）等原因导致调用失败，Hystrix能够保证在一个 服务出问题的情况下（调服务时调不同，那个服务可能处于宕机的状态，或者处于一直调不通的状态等等一些情况），不会导致整体服务失败（不会导致整个微服务失败，即如果存在有几十个微服务，它并不会影响到其他的微服务，也就是帮开发者进行了 熔断 操作，帮助开发者进行 熔断 降级 操作，做这个工作），避免 级联故障（服务雪崩），以提高 分布式系统的 弹性。

即存在有很多系统之间有服务的相互调用，微服务开发是由很多个子项目子系统进行构成的。

比如说该图当中存在有多个微服务，分别是微服务A、微服务B、微服务C、微服务D、微服务D、微服务E；微服务A调用微服务B，微服务B调用微服务C，微服务C调微服务D，微服务E调用微服务C。

那么如果微服务C宕机了的话，此时就有可能会产生一个问题即微服务E需要调用微服务C，所以此时微服务E会受到影响，且微服务B会需要调用微服务C，那么微服务B也会受到影响，微服务B和微服务E都会受到影响，当微服务C宕机之后，微服务E当中的线程请求过来需要调用微服务C，而因为微服务C宕机的缘故就需要进行等待，等待到它这个线程请求超时为止，一直调不通，那通过网络一直连接连接，一直到连接超时，那么微服务E当中的线程就会一直在等微服务B基于微服务E的响应结果，即网络连接超时，那么微服务E当中的线程就会被卡住了，与此同时微服务E当中又来了很多的请求，所以就会多个线程需要请求微服务B，而由于微服务B宕机缘故，那么这些大量线程请求服务都会被卡住，即很多的线程将在微服务E当中被卡住，因为前端还有可能有更多 的请求不停的发到微服务E当中来，不停的在调微服务E当中的服务，那么此时微服务E到时候就会堆积线程越来越多，就有可能造成微服务E的宕机。

这个时候微服务E宕机之后，前端可能又有一个项目也有可能宕机，即微服务F，而该微服务F是调用微服务E的，而微服务F上还有微服务G，微服务G调用微服务F。这样就产生了级联的这种问题。到时候级联就会引发微服务E、微服务F、微服务G发生宕机问题。即微服务G、微服务F、微服务E都访问不了了，很多的线程被卡住了，由于微服务B所在的服务器没有给到微服务E响应，再加上前端的请求越来越多，堆积越来越多，比如说微服务G，jvm溢出、xx异常等等之类的问题原因导致微服务G也宕机了。

那么此时就产生了级联的效果，即单个服务出现问题之后，级联多个服务，多个服务受到影响。

即微服务C宕机了，可能将导致微服务B宕机，微服务B宕机了之后可能导致微服务A宕机。

微服务D不会进行宕机，微服务C不再调用微服务D，所以微服务D不会出现宕机的问题。这样一个服务的宕机就将在多个服务调用之间由于网络超时+客户端请求量大导致线程因为等待宕机服务的网络超时响应而卡死。即导致宕机链越来越大，很多的机器都受到了影响。这就是所说的微服务分布式开发，一个微服务出现问题就有可能导致多个其他服务的问题。原因就在于，一个线程过来了之后由于对方服务宕机响应网络超时而导致卡住以及线程请求量越来越多，线程量堆积越来越多，就有可能导致服务宕机。

因为服务E在请求服务C的时候请求不通，所以微服务E就会进行等待服务C的响应结果。接着又有很多的请求会过来，从而堆积了很多请求，从而会导致这样的问题。

那么这个问题就是用 Hystrix来进行解决这样一个问题，它可以进行解决这样的问题，可以避免服务发生雪崩的问题。

![image-20210522115819985](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210522115819985.png)

熔断器 也有叫 断路器，它们表示同一个意思，最早来源于 微服务之父 Martin Fowler的论文 CircuitBreaker 一文。

“熔断器”本身是一种 开关装置，用于在 电路上 保护线路过载，当线路中有 电路发生短路时，能够及时切断故障电路，防止发生过载、发热甚至起火等严重后果。

借鉴了生活当中的一个东西叫做保险丝，即电路的保险丝，如果电流过大，功率太大，那么会导致保险丝的熔断。即跳闸了。

那么这里所说的熔断器就是说的是当xx过大那么它就可以自动帮助开发者实现跳闸这一功能。

所以当 某个服务单元发生故障之后，通过 断路器的 故障监控（类似 熔断保险丝），向 调用方 返回一个 符合预期的、可处理的备选响应（FallBack），而不是长时间的等待 或者 抛出调用方 无法处理的异常（它不会进行抛出异常只是基于消费方返回给消费方一个备选默认的结果，这个也称之为 服务降级），这样就保证了 服务调用方的线程不会被长时间地占用，从而避免了 故障在分布式系统中的蔓延，乃至雪崩。（在调用的时候，如果后端服务调用不通，在预定的一个事件内，如果没有响应，那么就直接进行熔断了，熔断了那么这个时候微服务当中的线程就结束了，它不会去进行占用线程资源 ）

比如：

比如电商中的 用户下订单，我们有两个服务，一个下订单服务，一个减库存服务，当用户下订单时调用 下订单服务，然后 下订单服务 又调用 减库存服务，如果 减库存服务 响应延迟或者没有响应（或者宕机了），则会造成 下订单服务的 线程挂起等待，如果大量的用户下单，或者导致大量的请求堆积，引起下订单服务也不可用，如果还有 另外一个服务依赖于 订单服务，比如用户服务它需要查询用户订单（加重了订单服务的压力），那么用户服务查询订单 也会引起大量的延迟和请求堆积，导致用户服务也不可用。

因为库存服务不可用了导致下订单服务不可用，订单服务不可用导致用户服务不可用。这是故障的蔓延。

所以在 微服务架构中，很容易造成 服务故障的蔓延，引发整个微服务系统瘫痪不可用。引发服务的雪崩。

基于这个情况这个问题，Spring Cloud Hystrix 实现了 熔断器、线程隔离 等一系列服务保护功能。

该功能也是基于 Netflix 的开源框架 Hystrix 实现的，该框架的目标在于通过控制那些 访问远程系统、服务和第三方库的结点，从而对延迟和故障提供更强大的容错能力。

Spring Cloud Hystrix就是用来解决这一系列的类似问题。进行解决微服务当中的问题。也就是Spring Cloud Hystrix对Netflix的开源框架 Hystrix进行了包装，Hystrix也是Netflix公司提供的。Spring Cloud Hystrix对Netflix的Hystrix做了二次包装，封装。

以上为对Hystrix做了一个介绍，即Hystrix就类似于像保险丝一样，最早是Netflix公司开源的一个组件，Spring Cloud对该组件进行一次封装，做了封装其实是做了一个starter，spring cloud其实就是对Hystrix做了一个starter的封装。

因为之前在讲解spring boot的时候说过，Spring Cloud Hystrix对Netflix Hystrix做一个starter即可，启动器。

Spring Cloud Hystrix 对 Netflix Hystrix 做了一个 starter启动器，然后在springboot当中进行开发就可以直接使用了。不然自己去进行开发就很麻烦，需要自己去包装该Netflix Hystrix ，所以让开发者应用十分方便。

（Spring Cloud Hystrix 对 Netflix Hystrix 做了一个 starter）

上述中介绍完Hystrix之后知道Hystrix是一个什么东西之后，那么接下来去进行使用Hystrix。

## 022_Spring Cloud Hystrix 应用服务降级-1

**程序BUG、数据不匹配、响应时间过长，服务不可用**等等原因都可能导致服务雪崩。

针对上面的问题，Hystrix提供了：

**熔断降级**

**请求限流**

**熔断降级** 是指当某个微服务 响应时间过长，或者是发生异常，或者服务不可用了，我们不能把错误信息返回回来（不能把错误信息直接返回给前台），或者让它一直卡在那里（不能这样做），所以要准备一个对应的策略（一个方法，写一个方法，该方法即为降级的方法），当发生这种问题时（即响应时间过长、发生异常、服务不可用时），我们直接调用这个备用（备选）的方法（给用户）来快速返回一个默认的结果，让请求得到快速响应，而不是一直卡在那里。

（如果一直卡在那里就会导致线程堆积，线程堆积就会可能会导致服务雪崩。）

### 操作步骤

在SpringCloud 中使用 熔断器Hystrix 是非常简单和方便的（spring cloud Hystrix提供了启动器叫starter，spring-cloud-starter-netflix-hystrix，即其实为spring boot starter原理，帮开发者将第三方的组件进行封装起来了，然后添加该组件在spring cloud当中的starter启动器即可使用），只需要简单两步即可：

1. 加依赖

   ```XML
   <!--spring-cloud-starter-netflix-hystrix-->
   <dependency>
       <groupId>org.springframewor.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   ```

2. 在入口类中使用 `@EnableCircuitBreaker` 注解或 `@EnableHystrix` 开启 断路器功能，也可以使用一个名为 `@SpringCloudApplication`的注解代替 主类上的三个注解;

3. 在调用远程服务的方法上 添加注解：

   ```java
   @HystrixCommand(fallbackMethod = "fallback")
   ```

   **hystrix 默认超时时间是 1000 毫秒**，**如果你后端的响应超过此时间，就会触发断路器**（触发服务降级）；（HystrixCommand 相当于是Hystrix的命令，该HystrixCommand注解当中存在有一些参数，最主要是指定fallbackMethod，即如果远程服务不可用或者远程服务超时或者异常了我们怎么办怎么处理的一个方法，则调用"fallback"该方法，fallback作为兜底的方法，兜底即相当于容错）

   修改hystrix 的默认超时时间：

   ```java
   @RequestMapping("/cloud/goodsHystrix")
   @HystrixCommand(fallbackMethod = "fallback", commandProperties={
       @HystrixProperty(name="execution.timeout.enabled", value="true"),
       @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="5000")
               
   })
   public ResultObject goodsHystrix() 
   ```

   或者

   ```properties
   ribbon.ReadTimeout=6000
   ribbon.ConnectTimeout=3000
   
   hystrix.command.default.execution.timeout.enabled=true
   hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000
   ```

   坑：

   如果 `hystrix.command.default.execution.timeout.enabled`为**true**，则会**有两个执行方法超时的配置**，一个就是 `ribbon`的 `ReadTimeout`（读取超时），一个就是熔断器 `Hystrix` 的`timeoutInMilliseconds`，**此时谁的值小 谁生效**；

   如果 `hystrix.command.default.execution.timeout.enabled`为 `false`，则熔断器不进行 超时熔断，而是根据 `ribbon`的 `ReadTimeout`抛出的异常而熔断，也就是取决于 `ribbon`的 `ConnectTimeout`，配置的是请求服务的超时时间，除非服务找不到 或者 网络原因 这个时间才会生效。

   ```properties
   ribbon.ReadTimeout=6000
   ribbon.ConnectTimeout=3000
   
   hystrix.command.default.execution.timeout.enabled=true
   hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000
   ```

之前已经搭建好的服务

* 34-springcloud-service-commons
* 34-springcloud-service-eureka
* 34-springcloud-service-goods9100
* 34-springcloud-service-goods9200
* 34-springcloud-service-parent
* 34-springcloud-service-portal

接下来将上述的步骤早前端项目portal服务当中进行操作一下。portal就相当于是消费者，即前端项目。然后消费者到时候就会去调用服务提供者goods9100或者是goods9200的服务，产品服务。

现在在前台portal服务当中要做一个服务的熔断，如果服务提供者goods9100或者是goods9200到时候在服务消费者调用的时候没有给予正常的响应，那么此时在服务消费者这边就需要提供给用户一个页面，一个兜底的数据，即做一个服务的降级。

那么第一步即在portal服务项目当中的pom.xml中加上hystrix的依赖启动器`spring-cloud-starter-netflix-hystrix`







![image-20210522152326990](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210522152326990.png)

在选中代码`goodsRemoteClient.goods()`，然后快捷键Ctrl+U/点击计算器图标之后，会弹出弹窗Evaluate，在弹窗中直接点击按钮 Evaluate即可。

![image-20210522152446279](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210522152446279.png)

可以看到结果如下：

![image-20210522152534400](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210522152534400.png)

在这当中可以看到异常：`Method threw 'feign.FeignException$NotFound' exception.`

发生404错误：`feign.FeignException$NotFound: status 404 reading GoodsRemoteClient#goods()`

所以此时才会触发@HystrixCommand的服务降级调用了fallback()，即调用远程服务中出错也是会进行降级处理。

此时可以知道后端方法是有问题的。导致了报错。并不是源于hystrix默认超时一秒钟进行服务降级的原因。

```bash
result = {FeignException$NotFound@10357} Method threw 'feign.FeignException$NotFound' exception.
 status = 404
 content = {byte[137]@10361} 
 detailMessage = "status 404 reading GoodsRemoteClient#goods()"
 cause = {FeignException$NotFound@10357} "feign.FeignException$NotFound: status 404 reading GoodsRemoteClient#goods()"
 stackTrace = {StackTraceElement[48]@10363} 
 suppressedExceptions = {Collections$UnmodifiableRandomAccessList@9966}  size = 0
```



## 023_Spring Cloud Hystrix应用服务降级-2

```bash
Hot Swap failed.
			GoodsApplication9100: schema change not implemented
			GoodsApplication9100: Operation not supported by VM
			GoodsApplication9200: schema change not implemented
			GoodsApplication9200: Operation not supported by VM
```

上述为idea提示，即刚添加完spring boot热部署插件就会产生有的红色提示，不影响正常开发测试使用。

![image-20210522214544590](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210522214544590.png)

添加有自动热部署插件依赖的服务后面会接一个`[devtools]`的备注；

```xml
<!--spring boot 开发 自动热部署-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
        </dependency>
```



Hystrix的默认超时时间是1000毫秒，即1秒钟，时间太短了，平时项目开发时，随便查询一个数据库可能就超过1秒钟了。那么这样的话，可以进行修改Hystrix的默认超时时间。通过配置

```properties
feign.hystrix.enabled=true
hystrix.command.default.execution.timeout.enabled=true
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000
```



## 025_Spring Cloud hystrix异常处理

### Hystrix 的异常处理

























