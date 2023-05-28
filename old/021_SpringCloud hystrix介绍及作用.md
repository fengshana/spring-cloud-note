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

Hystrix的异常处理

我们在调用 服务提供者的时候，（服务提供者可能会抛出异常）我们自己也有可能会抛异常，默认情况下 方法抛了异常 会自动进行服务降级，交给服务降级 中的方法去处理；

当我们自己发生异常后，只需要在服务降级方法中 添加一个 Throwable 类型的参数就能够获取到 抛出的异常的类型（也就是说指的是可以拿取得到异常的信息），如下:

```java
public ResultObject fallback(Throwable throwable){
    System.out.println(throwable.getMessage());
    return new ResultObject(Constant.ONE, "服务降级");
}
```

当然 远程服务 发生了异常 也可以获取到 异常信息；

即在降级方法fallback()当中可以直接添加方法参数Throwable throwable。

也就是在portal服务当中的controller中的fallback()方法加一个方法参数Throwable throwable。



如果远程服务有一个异常 抛出后，我们不希望进入到 服务降级方法 中去处理，而是直接将异常抛给用户，那么我们可以在 @HystrixCommand 注解中 添加 忽略异常，如下：

```java
@HystrixCommand(fallbackMethod='fallback', ignoreException=Throwable.class)
```

即比如说不想被hystrix的throwable给拦截掉，然后再走服务降级了，即不想服务降级，想要直接抛出异常，即可以在@HystrixCommand注解中配置项ignoreException即忽略异常。

ignoreException中的异常类.class 该异常类可以写Exception.class也可以写RuntimeException.class等等都可以。

Throwable是最顶层的异常父类。  

在配置了@HystrixCommand注解的ignoreException之后，此时hystrix就不会进行服务降级了，不会理会远程服务的异常的，即将远程服务的异常直接抛给前台。



### 降级的作用？

1. 可以监听你的请求有没有超时；（默认超时时间是1秒，超时时间可以修改ribbon.ReadTimeout+hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds两个配置一起）

2. 异常 或  报错了 可以快速让请求返回，不会一直等待；（避免线程等待，避免线程累积从而服务宕机，很多线程都挂起了没有响应出了问题；fallbackMethod的设置）

3. 当系统马上迎来大量的并发（双十一秒杀 这种 或者促销活动，流量比较大的时候），此时如果系统承载不了这么大的并发时，可以考虑先关闭一些不重要的微服务（在降级方法中返回一个比较友好的信息），把资源让给核心微服务，待高峰流量过去，再开启回来。

   即远程有一个服务，有一个消费者，然后远程服务有两个服务提供者产品服务，一共有四个服务，再就是两个周边的服务，给其他服务发消息通知的一个服务，比如说9点到12点是一个流量高峰期，机器自己的服务承受不了这么大的流量时，完全就可以将周边服务关闭了都没有事情，即直接将周边服务的tomcat给停掉关掉，由于消费者服务当中有hystrix，如果在消费者访问周边服务的时候，发现周边服务不可用，即周边服务停掉了，那么此时消费者当中的hystrix就会走服务降级。这样的话也不会影响前端。

   相当于关闭周边服务之后，用户在点击我的消息时，就提示一个友好的信息，比如说当前系统繁忙，请稍后再次尝试等信息。即完全可以将该周边服务关闭掉，如果说流量比较大的时候，不要影响主服务的流程操作。主要服务不要关闭，要保证消费者服务可以正常访问主服务接口。	

   远程服务不可用的时候hystrix就直接会走远程降级。这就是服务降级带给我们的效果作用。

hystrix.command.default.execution.timeout.enabled的默认取值是true，可以不用进行配置，可以在配置文件或者是注解当中省略该配置项 。



### Hystrix 限流

上面说的是hystrix在远程服务超时、异常、服务不可用的时候的一些处理，即可以自动进行服务降级等做一些处理。hystrix还有一种功能即可以帮助开发者进行限流的操作。限流有很多的办法。



限流有很多方案：

1. Nginx 限流
2. Redis + Lua脚本 限流
3. Alibaba Sentinel限流
4. 基于**限流算法**自己实现（**令牌桶**、**漏桶**算法去做限流）
5. Hystrix 限流

限流 就是限制某个微服务的 使用量(请求量)（可用线程）

**Hystrix**可以基于**可用线程数**、**信号量**这两个东西来做**限流**。

上面看到的是hystrix 在远程服务超时、服务不可用、异常等情况的时候，hystrix的处理，可以自动帮消费者服务portal进行服务降级。

除此之外hystrix还可以做限流的操作。

限流有很多种做法。

redis lua脚本限流，或者等等一些方式进行限流。还可以使用nginx做限流。等等。

分布式限流有好几种方案，比如说nginx限流，alibaba有一个sentinel限流，还有redis lua脚本可以限流，有一些的办法。

hystrix也可以做限流操作。

hystrix通过 **线程池**的方式来管理 微服务的调用，它默认是一个线程池（大小10个，这个数量可以进行配置）管理所有的微服务，可以给某个微服务开辟新的线程池：

```java
@RequestMapping("/cloud/goodsHystrix2")
@HystrixCommand(fallbackMethod="fallback",
               threadPoolkey="goods",
               threadPoolPerperties={
                   @HystrixProperties(name="coreSize", value="2"),
                   @HystrixProperties(name="maxQueueSize", value="1")
               })
public ResultObject goodsHystrix2() throws InterruptedException{
    
}
```

其中 maxQueueSize 是一个线程队列，里面只能放一个请求线程，假设本来线程数有2个，队列里面允许放一个，那么总共只能有3个请求线程，如果超过了就会限流。



threadPoolKey 是线程池唯一标识，hystrix 会使用该标识来计数，看线程占用是否超过了，超过了就直接降级该次应用。

在方法goodsHystrix2上面也是添加@HystrixCommand注解，在该注解当中有很多的参数，fallbackMethod是降级方法，然后还可以进行指定线程数，即threadPoolKey、threadPoolProperties属性的配置

比如，这里coreSize 给它值 为2，那么假设这个方法调用时间是1s执行完，那么在1s内如果有超过2 个请求进来的话，剩下的请求则全部降级。 

也是在@HystrixCommand注解上面进行添加配置项内容。

@HystrixCommand注解有很多的参数，fallbackMethod是降级的方法，然后还可以指定**线程数**。线程数的配置是 threadPoolKey、threadPoolProperties。



### feign 整合 hystrix

也就是feign和hystrix两者搭配在一起使用。其实在goodsHystrix()方法当中已经使用到了，底层就是使用的feign去调用的远程服务即goodsRemoteClient.goods();

下面所说的feign和hystrix整合的意思是，到时候会将代码做一下改造即到时候服务降级不再是在controller当中去写了，也就是不再在portal服务当中的GoodsController当中写fallback()方法了。

而是在接口层当中也就是feign声明式服务调用当中的那个FeignClient当中，也就是GoodsRemoteClient当中去写，声明式的feign的接口当中去写。也就是在GoodsRemoteClient当中去添加服务降级，fallback。

feign 默认是 支持 hystrix的，但是在 spring cloud dalston 版本之后就默认关闭了，因为不一定业务需求要用得到。

所以现在要使用首先得打开它，在yml文件加上如下配置：

```properties
feign.hystrix.enabled=true
```

加上配置之后降级方法怎么写呢？

```java
@FeignClient(value="34-SPRINGCLOUD-SERVICE-GOODS", fallback = GoodsRemoteClientFallBack.class)
public interfalce GoodsRemoteClient{
    /*
    声明一个feign的接口，它的实现是服务提供者的controller实现
    */
    @RequestMapping("/service/goods")
    public ResultObject goods();
}
```

feign的声明在commons服务当中。

在feign客户端的注解上，有个属性叫fallback，然后指向一个类 GoodsRemoteClientFallBack类：

```java
@Component
public class GoodsRemoteClientFallBack implements GoodsRemoteClient{
    
    @Override
    public ResultObject goods(){
        return new ResultObject(Constant.ONE, "feign服务调用降级");
    }
}
```

如此方法降级便可以了。

当然如果需要拿到具体的服务错误信息，那么可以这样：

```java
@Component
public class GoodsRemoteClientFallBackFactory implements FallbackFactory<GoodsRemoteClient>{
    @Override
    public GoodsRemoteClient create(Throwable throwable){
        return new GoodsRemoteClient(){
            @Override
            public ResultObject goods(){
            String message = throwable.getMessage();
                System.out.println("feign远程调用异常："+message);
                return new ResultObject();
            }
        }
    }
    
}
```

客户端指定一个 fallbackFactory即可

```java
@FeignClient(value = "34-SPRINGCLOUD-SERVICE-GOODS", fallbackFactory = GoodsRemoteClientFallBackFactory.class)
public interface GoodsRemoteClient{
    
}
```





通过feign的方式在@FeignClient当中的配置项fallback配置服务降级类GoodsRemoteClientFallBack.class，而该服务降级类当中实现GoodsRemoteClient feign接口中的方法goods()方法，该方法即为服务降级方法。

而这个时候想要去那远程服务的异常信息，就需要如上配置。

这个message就是错误信息，至此，就完成了feign与hystrix的整合。

将feign和hystrix结合，结合起来可以实现在feign的接口上（GoodsRemoteClient），用feign本身做服务降级（@FeignClient注解当中配置fallback、fallbackFactory配置项）

不是在controller当中，而是在feign接口层做服务降级，在feign的声明式接口当中做服务降级

### Spring Cloud Feign 超时时间设置

Feign调用服务的 默认时长是1秒钟，也就是如果超过1秒没连接上 或者 超过1秒没响应，那么会相应的报错。

而实际情况是因为业务的不同可能出现超出1秒的情况，这是我们需要调整超时时间。

Feign的负载均衡底层用的就是ribbon。

在application.properties中添加如下配置，超过5秒没连接上报连接超时，如果超过5秒没有响应，报请求超时；

```properties
#参考RibbonClientConfiguration
#请求连接的超时时间，默认的时间为1秒
ribbon.ConnectTimeout=5000

#请求处理的超时时间
ribbon.ReadTimeout=5000
```

ribbon 还有 MaxAutoRetries 对当前实例的重试次数，MaxAutoRetriesNextServer 对切换实例的重试次数，如果ribbon的ReadTimeout超时，或者ConnectTimeout连接超时，会进行重试操作

由于ribbon的重试机制，通常熔断的超时时间需要配置得比 ReadTimeout长，ReadTimeout 比 ConnectTimeout长，否则还未重试，就熔断了

为了确保重试机制的正常运作，理论上（以实际情况为准）建议hystrix的超时时间为：（1 + MaxAutoRetries + MaxAutoRetriesNextServer）* ReadTimeout



超时时间测试：

1. ribbon需要指定ReadTimeout、ConnectTimeout需要指定时间
2. 然后还需要指定hystrix的默认超时时间execution.isolation.thread.timeoutInMilliseconds



### Hystrix 相关配置

Execution 相关的属性配置

| 属性名称                                                     | 描述                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| hystrix.command.default.execution.isolation.strategy         | 隔离策略，默认是Thread，可选 Thread\|Semaphore<br /><br />hystrix当中的限流策略，该策略当中默认是可用线程数的方式，Semaphore是指的是信号量，也可以指定成信号量的形式，用信号量Semaphore的方式也可以，Semaphore信号量的方式有点类似于令牌，去获取令牌，指定信号量比如说10个，如果获取到了信号量可以执行没有获取到则不能执行，相当于像令牌的含义一样。Thread选项则基于线程数。 |
| hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds | 命令执行超时时间，默认1000ms（默认执行超时时间为1秒，1000毫秒） |
| hystrix.command.default.execution.timeout.enabled            | 执行是否启用超时，默认启用，即true                           |
| hystrix.command.default.execution.isolation.thread.interruptOnTimeout | 发生超时时是否中断，默认true                                 |
| hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests | 最大并发请求数，默认10，该参数当使用 ExecutionIsolationStrategy.SEMAPHORE 策略时才有效。如果达到 最大并发请求数，请求会被拒绝。<br />理论上选择semaphore.size的原则和选择thread size一致，但选用 semaphore 时每次执行的单元要比较小且执行速度块（ms级别），否则的话应该用thread。<br />semaphore应该占整个容器（tomcat）的线程池的一小部分。<br />Fallback相关的属性，这些参数可以应用于Hystrix的THREAD 和 SEMAPHORE策略。<br />hystrix.command.default.fallback.isolation.semaphore.maxConcurrentRequest 如果并发数达到该设置值，请求会被拒绝和抛出异常并且fallback不会被调用。默认10 |
| hystrix.command.deafault.fallback.enabled                    | 当执行失败或者请求被拒绝，是否会尝试调用hystrixCommand.getFallback()。默认true |
|                                                              |                                                              |

一般情况下并不需要配置很多信息。

如果需要配置则可查询相关配置信息。

也就是类似SpringBoot一样，当中存在有很多很多的配置，但是可能就只需要配置其中的某一个部分。很多东西还是使用默认的。当需要使用某个工具的时候，需要进行开启某个工具的功能，那么这个时候就需要找一下相关的spring-该工具的配置；

可以看一下相关的配置

Circuit Breaker相关的属性

| 属性名称                                                     | 描述                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| hystrix.command.default.circuitBreaker.enabled               | 用来跟中circuit的健康性，如果未达标则让request短路。默认true |
| hystrix.command.default.circuitBreaker.requestVolumeThreshold | 一个rolling window内最小的请求数。如果设为20，那么当一个rolling window的时间内（比如说1个rolling window是10秒）说到19个请求，即使19个请求都失败，也不会触发circuit break。默认20 |
| hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds | 触发短路的时间值，当该值设为5000时，则当触发circuit break后的5000毫秒内都会拒绝request，也就是5000毫秒内才会关闭circuit。默认5000 |
| hystrix.command.default.circuitBreaker.errorThresholdPercentage | 错误比率阈值，如过错误率 > = 该值，circuit会被打开，并短路所有请求触发 fallback。默认50 |
| hystrix.command.default.circuitBreaker.forceOpen             | 强制打开熔断器，如果打开这个开关，那么拒绝所有request，默认false |
| hystrix.command.default.circuitBreaker.forceClosed           | 强制关闭熔断器，如果这个开关打开，circuit将一直关闭且忽略circuitBreaker.eoorThresholdPercentage |
|                                                              |                                                              |



### Metrics 相关参数

| 属性名称                                                     | 描述                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| hystrix.command.default.metrics.rollingStats.timeInMilliseconds | 设置统计的时间窗口值的，毫秒值，circuit break的打开会根据1个rolling window的统计来计算。若rolling window被设为10000毫秒，则rolling window会被分成n个buckets，每个bucket包含success、failure、timeout、rejection的次数的统计信息。<br />默认10000 |
| hystrix.command.default.metrics.rollingStats.numBuckets      | 设置一个rolling window被划分的数量，若numBuckets=10，rolling window=10000，那么一个bucket的时间即1秒。必须符合rolling window % numberBuckets ==0。默认10 |
| hystrix.command.default.metrics.rollingPercentile.enabled    | 执行时是否enable指标的计算和跟踪，默认true                   |
| hystrix.command.default.metrics.rollingPercentile.timeInMilliseconds | 设置rolling percentile window的时间，，默认60000             |
| hystrix.command.default.metrics.rollingPercentile.numBuckets | 设置rolling percentile window的numberBuckets。<br />逻辑同上。默认6 |
| hystrix.command.default.metrics.rollingPercentile.bucketSize | 如果bucket size=100，window=10s，若这10s里有500次自行，只有最后100次执行会被统计到bucket里去。增加该值会增加内存开销以及排序的开销。默认100 |
| hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds | 记录health快照（用来统计成功和错误率）的间隔，默认500ms      |



### Request Context相关参数

| 属性名称                                     | 描述                                               |
| -------------------------------------------- | -------------------------------------------------- |
| hystrix.command.default.requestCache.enabled | 默认true，需要重载 getCacheKey()，返回null时不缓存 |
| hystrix.command.default.requestLog.enabled   | 记录日志到HystrixRequestLog，默认true              |
|                                              |                                                    |



### Collapser Properties 相关参数

| 属性名称                                       | 描述                                                         |
| ---------------------------------------------- | ------------------------------------------------------------ |
| hystrix.collapser.default.maxREquestsInBatch   | 单词批处理的最大请求数，达到改数量触发批处理，默认Integer.MAX_VALU |
| hystrix.collapser.timerDelayInMilliseconds     | 触发批处理的延迟，也可以为创建批处理的时间+该值，默认10      |
| hystrix.collapser.default.requestCache.enabled | 是否对HystrixCollapser.execute() and HystrixCollapser.queue()的cache，默认true |
|                                                |                                                              |
|                                                |                                                              |
|                                                |                                                              |
|                                                |                                                              |



### ThreadPool相关参数

线程数默认值10，适用于大部分情况（有时可以设置得更小），如果需要设置得更大，那有个基本的公式可以follow：

request per second at peak when healthy × 99th percentile latency in seconds + some breathing room 每秒最大支持的请求数（99%平均响应时间 + 缓存值）

比如：每秒能处理1000个请求，99%的请求响应时间是60ms，那么公式：1000（0.060 + 0.012）基本的原则是保持线程池尽可能小，它主要是为了释放压力，防止资源被阻塞。

当一切都是正常的时候，线程池一般仅会有1到2个线程激活来提供服务

| 属性名称                                                     | 描述                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| hystrix.threadpool.default.coreSize                          | 并发执行的最大线程数，默认10                                 |
| hystrix.threadpool.default.maxQueueSize                      | BlockingQueue的最大队列数，当设为-1，<br />会使用SynchronousQueue，<br />值为正时使用LinkedBlockingQUeue。<br />该设置只会在初始化时有效，之后不能修改threadpool的queue size，除非 reinitialising thread executor。默认-1. |
| hystrix.threadpool.default.queueSizeRejectionThreshold       | 即使maxQueueSize没有达到，达到queueSizeRejectionThreshold该值后，请求也会被拒绝。<br />因为maxQueueSize不能被动态修改，这个参数将允许我们动态设置该值。<br />if maxQueueSize == 1，该字段将不起作用。<br /> |
| hystrix.threadpool.default.keepAliveTimeMinutes              | 如果corePoolSize和maxPoolSize设置一样（默认实现）该设置无效。如果通过plugin（https://github.com/Netflix/Hystrix/wiki/Plugins）使用自定义实现，该设置才有用，默认1 |
| hystrix.threadpool.default.metrics.rollingStats.timeInMilliseconds | 线程池统计指标的时间，默认10000                              |
| hystrix.threadpool.default.metrics.rollingStats.numBuckets   | 将rolling window划分为n个buckets，默认10；                   |
|                                                              |                                                              |



## Hystrix 仪表盘监控

接下来看Hystrix仪表盘监控。

Hystrix DashBoard相当于Springboot项目当中的有一个actuator监控，有点类似于这个，hystrix仪表盘dash board是对hystrix的监控，对熔断器的监控，所以称之为 hystrix Dashboard仪表盘。

Hystrix仪表盘（HystrixDashboard），就像汽车的仪表盘实时显示汽车的各项数据一样（当前汽车的速度是多少、油量还剩多少等等信息），Hystrix仪表盘主要用来监控Hystrix的实时运行状态，通过它我们可以看到Hystrix的各项指标信息，从而快速发现系统中存在的问题进而解决它。（也是一个监控功能，SpringBoot当中也有一个监控功能叫做actuator，Hystrix也有一个监控功能，即Hystrix Dashboard）

要使用Hystrix仪表盘功能，我们首先需要一个Hystrix Dashboard，这个功能，我们可以在原来的消费者应用上添加，让原来的消费者具备 Hystrix DashBoard仪表盘功能，

但一般地，微服务架构思想是推崇服务的拆分，Hystrix Dashboard也是一个服务，所以通常会单独创建一个新的工程专门用作Hystrix Dashboard服务；

前端项目portal服务复制一份，将名字改一下该名称为34-springcloud-service-dashboard，将该文件夹下的target目录删除。打开该文件夹下的pom.xml文件进行修改，修改内容如下：

```xml
 <groupId>com.bjpowernode.springcloud</groupId>
    <artifactId>34-springcloud-service-dashboard</artifactId>
    <version>1.0.0</version>



<name>34-springcloud-service-dashboard</name>
    <description>34-springcloud-service-dashboard project for Spring Boot</description>
```

修改完成之后将项目搭建起来，到时候做服务的监控。

在idea当中导入该dashboard服务。

IDEA----->File----->New------>Module from Existing Sources.....------->点击到34-springcloud-service-dashboard项目当中的pom.xml文件双击或者单击该pom.xml然后点击OK------>Next-------->Finish即可

dashboard用来做服务的监控，hystrix的监控。

dashboard模块导入进来之后先看依赖，





搭建一个Hystrix Dashboard服务的步骤：

第一步：创建一个普通的SpringBoot工程

比如创建一个名为 springcloud-hystrix-dashboard 的springboot工程，建立好基本的结构和配置；

第二步：添加相关依赖

在创建好的 Spring Boot项目中的pom.xml 中添加相关依赖，如下：

```xml
<!--该依赖已经过时，当前仅为展示知道一下，依赖搞不对也是会发生问题的，可到maven仓库当中去检索一下通过spring-cloud-starter-hystrix-dashboard

通过spring-cloud-starter-hystrix-dashboard到maven仓库当中去检索会提示内容如下：
Spring Cloud Starter Hystrix Dashboard(deprecated. please use spring-cloud-starter-netflix-hystrix-dashboard)

Spring Cloud Starter Hystrix Dashboard (deprecated, please use spring-cloud-starter-netflix-hystrix-dashboard)


deprecated 过时的，请使用spring-cloud-starter-netflix-hystrix-dashboard依赖
即原来的像F版本、D版本、E版本它们取名称不够标准，spring cloud后面把名字都进行了一下统一。
之前叫做spring-cloud-starter-hystrix-dashboard，现在叫做spring-cloud-starter-netflix-hystrix-dashboard
当中有添加一个-netflix，会发现当中有这样一个规律，会添加一个-netflix，所以此时就需要寻找依赖spring-cloud-starter-netflix-hystrix-dashboard，现在的名字已经变得规范了，原来的名字不规范，现在已经保持统一了，统一规范。
在maven仓库当中进行检索spring-cloud-starter-netflix-hystrix-dashboard该依赖；
如下

<! -- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-hystrix-dashboard -- >
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
    <version>2.2.8.RELEASE</version>
</dependency>
在dashboard服务的pom.xml当中并不需要写版本号，因为在父项目当中已经定义了版本。

在dashboard服务的pom.xml中可以将以下依赖进行删除
spring-cloud-starter-netflix-hystrix
spring-cloud-starter-netflix-eureka-client
34-springcloud-service-commons
再添加spring-cloud-starter-netflix-hystrix-dashboard
-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix.dashboard</artifactId>
    <version>1.4.5.RELEASE</version>
</dependency>


<!--新的依赖如下-->
<!--添加hystrix仪表盘监控依赖
        在该依赖当中并不需要定义版本号因为在父依赖当中已经定义好了版本号了
        -->
        <!-- spring-cloud-starter-netflix-hystrix-dashboard -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
            <!--
            <version>2.1.3.RELEASE</version>
            -->
        </dependency>
```

第三步：入口类上添加注解

添加好依赖之后，在入口类上添加@EnableHystrixDashboard注解开启仪表盘功能，如下:

```java
@SpringBootApplication
@EnableHystrixDashboard
public class Application{
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
    
}
```

第四步：属性配置

最后，我们可以根据个人习惯配置一下 appliaction.properties文件，如下：

```properties
server.port=3721
```

至此，我们的Hystrix监控环境就搭建好了；

![image-20210527141417108](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527141417108.png)

Hystrix仪表盘工程已经创建好了，现在我们需要有一个服务，让这个服务提供一个路径为 /actuator/hystrix.stream 接口，然后就可以使用 Hystrix仪表盘 来对该服务进行监控了；

我们改造消费者服务（因为服务消费者当中使用到了hystrix），让其能提供/actuator/hystrix.stream接口（然后将该接口输入到dashboard后台管理页面当中的 http://hostname:port/turbine/turbin.stream 该文本框当中去，然后点击`Monitor Stream`按钮从而实现对其服务中hystrix的监控），步骤如下：

1. 消费者项目需要有hystrix的依赖：

   ```xml
   <!--Spring Cloud 熔断器起步依赖
   
   该依赖spring-cloud-starter-hystrix也是过时的，新的依赖即添加了-netflix，即spring-cloud-starter-netflix-hystrix
   -->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-hystrix</artifactId>
       <version>1.4.5.RELEASE</version>
   </dependency>
   
   <!--新的 依赖-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   
   ```

   

2. 需要有一个 spring boot的服务监控依赖

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

   

3. 配置文件需要配置 spring boot监控端点的访问权限：

   ```xml
   management.endpoints.web.exposure.include=*
   ```

   这个是用来暴露endpoints的，由于endpoints中会包含很多敏感信息，除了health和info两个支持直接访问外，其他的默认不能直接访问，所以我们让它都能访问，或者指定

   ```xml
   management.endpoints.web.exposure.include=hystrix.stream
   ```

   

4. 访问入口 http://localhost:8081/actuator/hystrix.stream

   注意：

   这里有一个细节需要注意，要访问/hystrix.stream接口，首先得访问comsumer工程中的任意一个其他接口，否则直接访问/hystrix.stream接口时会输出一连串的ping:ping:......，先访问consumer中的任意一个其他接口，然后再访问/hystrix.stream接口即可；

5. 







当前看一下消费者服务portal

![image-20210527164729563](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527164729563.png)



![image-20210527164747199](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527164747199.png)

![image-20210527165334311](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527165334311.png)



![image-20210527165514744](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527165514744.png)



当前看是没有什么变化的因为数据太少了。只有当访问变化量差异大的时候才能看到变化

![image-20210527165928345](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527165928345.png)



如果公司需要进行对其监控的话，可以将其hystrix dashboard搭建一下，如果不需要监控的话也可以不需要搭建该玩意儿。

hystrix dashboard需要为其单独搭建一台微服务，部署在一台机器上，然后进行监控各个微服务，监控各个微服务当中hystrix的运行情况、状态，主要是对hystrix做一个监控。



回顾内容

今天主要讲解了hystrix当中的内容，这是spring cloud当中的一个组件

首先是认识 Hystrix是什么东西，它是一个熔断器，是Netflix公司提供的开发的开源的，然后Spring Cloud将其作为一个组件进行封装，二次封装，给我们做一个starter这样一个起步依赖即spring-cloud-starter-netflix-hystrix，让开发者使用变得更加方便。

这是hystrix。

然后hystrix怎么使用，以及hystrix用来解决什么样的，用来避免服务的雪崩这种情况，一个服务无法使用导致另外一个服务也不能使用，所以可以帮开发者进行熔断，然后做一个服务降级。还有一个是hystrix可以进行请求限流，请求限流可以基于线程数也可以基于信号量来做请求限流，

hystrix的使用

第一步添加hystrix的依赖即spring-cloud-starter-netflix-hystrix

第二步在启动入口类上（main方法所在类）使用@EnableCircuitBreaker注解/@SpringCloudApplication或者@EnableHystrix注解开启hystrix功能。

第三步测试在portal服务的controller接口上添加@HystrixCommand注解，该注解配置项有fallbackMethod，指定一下降级方法。降级方法指定好之后在降级方法当中写降级逻辑就可以了。然后当调用远程服务超时、异常、不可用等情况的时候，hystrix就帮助开发者走到这个指定的降级方法当中去执行对应的降级逻辑。给前台页面返回的是降级方法当中返回的响应内容、结果。这样就不会出现服务超时了导致线程一直被卡住，不会卡在那里。这当中存在有一个超时的坑，即在设置hystrix的默认超时时间时不仅需要修改ribbon.ReadTimeout还需要修改hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds这两个配置项，光指定hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds还无法生效，这是hystrix有关超时时间的设置。

然后就是Hystrix的异常处理。

调用远程服务异常，hystrix可以拿到远程服务调用异常的信息，直接在@HystrixCommand注解中的配置项fallbackMethod中指定的方法fallback()中添加方法参数Throwable throwable即可拿到异常信息。如果需要获取异常信息则通过这种方式去进行获取。如果想在调用远程服务时将远程服务的异常信息直接抛给前端页面则在@HystrixCommand注解当中添加配置项ignoreException=Throwable.class即可。通过ignoreException该配置项可以进行忽略异常。

降级有什么用？

1. 降级可以监听你的请求有没有超时（默认是1秒，时间可以改）
2. 异常或报错了可以快速让请求返回，不会一直等待；（避免线程累积，如果异常报错了可以快速返回一个兜底的数据，也就是一个降级的数据）
3. 当系统马上引来大量的并发（双十一秒杀这种或者促销活动），此时如果系统乘载不了这么大的并发时，可以考虑先关闭一些不重要的微服务（因为服务关闭之后就无法进行访问了，无法访问则直接降级了，在降级中返回一个比较友好的信息），让资源让给核心微服务，待高峰流量过去，再开起回来 。

Hystrix限流

限流有很多方案

1. Nginx
2. Redis+Lus脚本
3. Sentinel
4. 限流算法：基于限流算法自己实现（令牌桶、漏桶算法）

今天所讲的限流是通过hystrix来实现的。

hystrix基于线程数或者是信号量来进行限流操作。默认线程数是10个，也可以进行配置。在@HystrixCommand注解当中的配置项@threadPookKey、@threadPoolProperties，在@threadPoolProperties注解数组中指定coreSize、maxQueueSize的大小。这样的话即可限流，默认线程数是10个。通过该参数@threadPoolProperties指定限流。

然后下面就是当feign和hystrix结合起来的时候，即feign整合hystrix。可以在feign的接口上，即feign有一个声明式接口，在这个接口上可以实现服务降级，这样的话就不用在portal服务的controller当中去每一个方法都写@HystrixCommand注解配置fallbackMethod该配置项了。因为一个远程服务的controller接口有可能有很多的消费者项目在调用，所以将@HystrixCommand中的fallback提出来会更好一些。即写在feign声明式接口当中，由@FeignClient去进行配置配置项fallback，服务降级类，该服务降级类实现了feign声明式接口即重写了该声明式接口当中的goods方法，该重写过了的该方法即服务降级方法。

通过使用feign来做hystrix的服务降级，使得代码更加通用。因为在feign声明式接口当中做了服务降级之后，即fallback服务降级类之后，所有调用feign声明式接口当中方法的消费者服务controller，到时候都是可以进行服务降级的。这样更好一些，即通过feign来整合hystrix。这个时候当使用的是低版本以后 低版本则application.properties文件当中还需要开启一下配置项，该配置项在spring cloud dalston版本之后就默认关闭了即false

```properties
feign.hystrix.enabled=true
```

application.properties文件当中开启feign整合hystrix之后，然后在feign声明式接口GoodsRemoteClient接口上的注解@FeignClient中添加一个配置项fallback，该fallback配置项参数的取值是一个类，该类当中去进行做降级。然后在消费者的controller当中就不再需要写@HystrixCommand了，即不再需要通过@HystrixCommand注解去配置fallback指定降级方法了。这样的话代码会比较少一点精简一点。

feign整合hystrix还包括了拿取远程服务的异常信息。如果想要获取拿到远程服务的异常信息，则通过在feign声明式接口当中的@FeignClient注解中配置FallBackFactory配置项即可，该fallbackFactory配置项的取值也是一个类，该类实现了FallBackFactory<T\>接口，并重写了该接口当中的create方法，该方法的方法参数即为Throwable throwable，通过该throwable可以拿取到远程服务异常信息，在该方法当中返回feign声明式接口的对象，该对象即feign声明式接口的匿名内部类实例对象，在该匿名内部类当中需要重写feign声明式接口当中的goods()方法，重写的该方法即服务降级方法。

下面就是Spring Cloud feign的超时时间设置，有一个小坑。

hystrix相关配置，当中有很多大量的配置。

后续提到的就是仪表盘hystrix dashboard，可以对hystrix的运行情况做一个监控。针对该仪表盘hystrix dashboard可以单独搭建一个项目微服务，在这个项目当中去做仪表盘的监控。该项目搭建起来之后，在消费者当中即在有使用hystrix的服务当中，做一下改造，即也是需要配置一下的，即消费者服务pom.xml中添加spring-cloud-starter-netflix-hystrix-dashboard以及spring-boot-starter-actuator依赖以及application.properties文件当中添加management.endpoints.web.exposure.include=hystrix.stream该配置项暴露端点。再就是后面监控服务的时候即访问地址http://localhost:dashboard服务的端口号/hystrix即可，在第一个文本框当中输入消费者的路径即http://localhost:8080/actuator/hystrix.stream，然后点击按钮Monitor Stream即可看到监控页面。这样就可以对该消费者当中的hystrix的运行情况做一个监控，可以进行查看该消费者中hystrix运行情况是怎么样的。如果还有一个消费者绑定地址则在http://localhost:dashboard服务的端口号/hystrix 首页中的第一个文本框中输入 另外一个消费者的绑定地址路径，然后再点击Monitor Stream按钮即可监控到该另外一个服务消费者中hystrix的运行情况。

今天主要讲解了hystrix服务熔断降级的组件。

### hystrix dashboard仪表盘如何监测多个消费者项目当中的hystrix运行情况？

 如何监控多个，还需要搭建一个东西，这个东西叫做turbine，即在http://localhost:3721/hystrix中再结合一下turbine，就可以在一个页面当中展示多个消费者服务的hystrix的运行情况，即在原本搭建好的hystrix dashboard仪表盘的基础上再加上一个turbine即可实现。

添加turbine即添加一下其依赖整合一下即可。

今天晚上主要讲解了hystrix做服务熔断，因为在微服务当中，有很多的服务，而服务与服务之间相互调用，如果有一个服务出现问题可能会导致请求线程累积，会拖累另外其他的服务，所以当中就做了一个方案叫做服务熔断。当然这个组件在开发的时候不进行使用行不行？行的。也是可以的。也可以用。那么hystrix主要提供给开发者的就是服务降级、熔断降级还有就是限流，根据线程数或者是信号量来进行降级，默认是根据线程数来进行限流，10个线程数默认。

这就是hystrix的功能，hystrix这个组件。所以spring cloud当中就是由一大堆的组件构成的，一个个的组件，可以这么简单的认为，一个组件就是spring cloud帮助开发者封装的一个starter，就是一个starter，之前在将spring boot的时候，介绍过starter的启动原理，spring cloud其实就是将netflix公司的jar包然后做了一个包装，制作成了一个starter，然后使用springboot就可以进行开发，使用很方便，加入了starter，做一个简单的配置，然后就可以使用这个组件的功能。包括后面还会使用的一些组件，分布式配置，链路追踪都能等一些功能，还有Spring Cloud batch等等，那么这些都是整合的一些组件，springcloud当中就是一大堆的组件构成的。

后续在这些组件讲完之后会使用一个综合案例把这些组件串连一下，相当于做一个小小的项目，功能没有那么多主要是用那个技术，将这些组件串起来。现将所有的组件讲完然后后面用一个项目案例一样，有几个功能，然后把springcloud中的组件添加进来，加进来之后然后去做这些功能。	



以上涉及到的测试地址有：

```bash
#访问hystrix dashboard仪表盘后台管理地址页面
http://localhost:3721/hystrix

http://localhost:3721/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A8080%2Factuator%2Fhystrix.stream

#消费者端路径（访问之前需要访问某一个消费者端的接口，否则出现ping:......，如果访问了则出现一大堆的json数据）
http://localhost:8080/actuator/hystrix.stream

#消费者访问测试地址
http://localhost:8080/cloud/goodsRibbon

#消费者访问测试地址
http://localhost:8080/cloud/goodsHystrix

#消费者访问测试地址
http://localhost:8080/cloud/goodsFeignHystrix
```





## 032_Spring Cloud Hystrix turbine服务构建

继续在已有的基础之上讲解微服务专题-一站式微服务架构SpringCloud

### 1、快速回顾

1. 分布式与微服务架构的理论梳理；（软件发展的过程，架构的发展历程做了一个梳理）
2. 什么是SpringCloud？（SpringCloud是微服务一站式的解决方案，SpringCloud由很多很多的组件构成）
3. Spring Cloud的整体架构（与Dubbo比较）（SpringCloud的整体架构可以和Dubbo做一个比较，和Dubbo的架构非常相似，是分布式微服务的一个解决方案，有三个部分，有提供者、消费者以及注册中心，核心在这三个部分）
4. 服务消费者Controller直连调用服务提供者Controller（http协议的resultful）（SpringCloud当中的调用是controller调用controller，基于http协议的restful这种调用，此处也可以进行与dubbo进行比较，dubbo是基于dubbo协议，基于rpc的远程调用，它们之间调用方式不一样，走的协议，协议也不一样）
5. Spring Cloud的注册中心Eureka（SpringCloud当中注册中心使用Eureka，那么在Dubbo当中注册中心主要使用Zookeeper）
6. Spring Cloud Eureka与Zookeeper比较（Eureka和Zookeeper这两个注册中心它们的关注点不一样，Eureka保证可用性以及分区容错性即AP，Zookeeper保证一致性以及分区容错性即CP）
7. Spring Cloud Eureka高可用集群（做三个节点或者多个节点，保证注册中心的高可用，在dubbo当中也是一样，zookeeper也会做一个集群，做多个节点，即集群）
8. Spring Cloud Eureka自我保护机制（避免服务因为网络分区而导致服务没有向注册中心发送心跳，那么此时自我保护机制可以将该服务保护起来，避免误杀某些服务，其实某些服务没有宕机，eureka如果没有自我保护机制的话会误认为某些服务宕机了，那么这样的话就会影响服务之间的调用，eureka比较关注可用性，不是关注一致性）
9. Spring Cloud Ribbon负载均衡（在调用的时候有一个ribbon实现负载均衡的调用，也是一个组件，是netflix公司提供的组件进行负载均衡调用）
10. Spring Cloud Feign声明式服务调用（与Dubbo接口层比较）（然后就是Feign该组件，也是netflix公司提供的，它是一种声明式的服务调用，可以调用远程的服务，写一个接口即可，与dubbo当中的接口层可以做一个比较，有点类似dubbo当中开发的时候写的一个接口层，Feign该组件也是netflix公司提供的）
11. Spring Cloud Hystrix服务熔断降级、服务限流（hystrix该组件也是netflix公司提供的，spring cloud对hystrix做了一个封装，做了一个starter，然后它可以实现熔断降级、服务限流，可以实现这些功能，在微服务开发的时候可能就需要用到）
12. Spring Cloud Hystrix DashBoard仪表盘监控（hystrix有一个仪表盘监控即dashboard，可以将该服务部署一下，该服务不进行部署也不影响，因为dashboard主要就是一个监控的功能，有点类似于springboot当中的actuator，是用于监控的）

### 2、Spring Cloud Hystrix Turbine

在仪表盘hystrix DashBoard的基础上介绍hystrix turbine。

Hystrix DashBoard前面已经知道了，它的主要功能是可以对某一项 微服务进行监控，但真是情况下，不可能只对一个微服务进行监控，我们有很多微服务，所以我们需要对很多微服务进行监控，这个时候就需要使用到 turbine 

单个hystrix服务的监控：

![image-20210527213117802](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527213117802.png)

在右边的这个地方是hystrix dashboard，即部署了一个服务，hystrix dashboard，它用来监控/hystrix.stream这个地址，这个接口地址，而这个接口地址是通过有一个服务，从这个服务当中获取的，那么这个服务需要做一些相关的配置，然后到时候就可以暴露出这么一个接口地址/hystrix.stream接口。然后hystrix dashboard服务就通过这个接口来对这个服务的监控。

而这个服务有可能调用另外的服务eureka client1、eureka client2这种，这些服务都可能会被注册到注册中心eureka server上

那么以上是hystrix dashboard对单个服务的监控，dashboard监控eureka-consumer-ribbon-hystrix该服务。

那么如果当hystrix服务有很多个的时候，那么此时就不一样了，看下图：

多个hystrix服务的监控：

![image-20210527213606505](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527213606505.png)

比如说，现在有多个hystrix服务（即eureka-consumer-ribbon-hystrix服务），该图当中即画了两个hystrix服务，eureka-consumer-ribbbon-hystrix（上）为用到了hystrix的服务，eureka-consumer-ribbon-hystrix（下）也是使用到了hystrix的服务，最右边是dashboard服务，然后这个时候dashboard怎么去监控那两台使用到了hystrix的服务呢？

hystrix dashboard通过Turbine，hystrix dashboard监控Turbine即可，然后Turbine就会去聚合eureka-consumer-ribbon-hystrix（上）和eureka-consumer-ribbon-hystrix（下），即eureka-consumer-ribbon-hystrix（上）和eureka-consumer-ribbon-hystrix（下）到时候就会在Turbine处做一个汇总，汇总一下，汇总到Turbine，而hystrix DashBoard就通过Turbine的接口/turbine.stream这个接口来进行监控那两个使用了hystrix的服务。那么hystrix dashboard在监控的时候即eureka-consumer-ribbon-hystrix（上）和eureka-consumer-ribbon-hystrix（下）这两个服务的信息就都有了。

eureka-consumer-ribbon-hystrix（上）和eureka-consumer-ribbon-hystrix（下）通过Turbine做一下聚合，然后展示到hystrix dashboard当中。

hystrix dashboard就是一个页面，该页面上即有一个图形的展示。展示该项目当中hystrix服务运行的状态，主要是hystrix的运行状态。

所以多个服务的时候，hystrix dashboard需要进行监控则通过Turbine来做聚合，就相当于汇合一下汇总一下，汇总之后就可以了，进行监控服务。

搭建一下turbine并使用它做聚合。

微服务开发，像turbine这个地方就是一个服务，就需要为turbine该服务搭建一个模块。即准备一个模块专门做turbine服务的。（该启动的东西都启动一下linux当中的高可用eureka集群、mysql服务、idea，都启动之后进行搭建turbine服务，要准备一个微服务，turbine该服务也是使用springboot进行开发的，所以在这边直接IDEA----->File-------->Module....--------->Spring Initializr------->）

具体步骤：

首先准备一个turbine模块

1、创建一个34-springcloud-service-turbine项目，该项目依然是一个springboot项目；

![image-20210527215014067](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527215014067.png)

![image-20210527215045606](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527215045606.png)

![image-20210527215128334](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527215128334.png)

所以在学习spring cloud之前需要学习spring boot。

由于老师那边通过spring initializr创建失败了，这边和他同步。

即可以通过本地磁盘直接拷贝一份项目，直接改一改即可。

即idea当中点击任意一个项目右击选择`Show in Explorer`打开该项目所在的磁盘工作空间。

![image-20210527215401004](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210527215401004.png)

将portal服务直接复制粘贴一份类似于dashboard服务一样处理。文件夹命名为`34-springcloud-service-turbine`

进入到turbine服务文件夹下，删除target不需要的文件。然后双击pom.xml文件进行修改内容如下：

```xml
<artifactId>34-springcloud-service-turbine</artifactId>

<name>34-springcloud-service-turbine</name>
    <description>34-springcloud-service-turbine project for Spring Boot</description>

```

修改一下名称即可。将portal替换成turbine即可。然后通过idea导入该turbine服务。 idea----->File-------->New--------->Module from Existing Sources.....--------->刷新后选择中turbine服务的pom.xml双击或者单击之后选择OK--------->Next------>Finish

此时还需要在parent服务当中的pom.xml当中的modules节点当中添加节点如下：

```xml
<modules>
	<!--.......-->
    <module>../34-springcloud-service-turbine</module>
</modules>
```

修改turbine服务当中的PortalApplication为TurbineApplication。



2、添加依赖(即添加turbine的依赖 spring-cloud-starter-netflix-turbine，该依赖前缀又是spring-cloud-starter 一个启动器，spring cloud帮助开发者将turbine做了一个包装，turbine也是netflix公司提供的，所以spring cloud官方那一套微服务解决方案在国内称之为spring cloud netflix，另外还有一套称作spring cloud alibaba，因为spring cloud alibaba这一套是从alibaba出来的，目前学习使用的组件都是spring cloud netflix这一套，是netflix公司的相关产品，springcloud做了一个整合即制作spring-cloud-starter启动器)：

```xml
<!--spring-cloud-starter-netflix-turbine-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
</dependency>
```



3、配置文件

```properties
server.port=3722

eureka.client.register-with-eureka=false
#eureka注册中心的连接地址
eureka.client.service-url.defaultZone=http://192.168.177.128:8761/eureka,http://192.168.177.128:8762/eureka,http://192.168.177.128:8763/eureka

#配置turbine
turbine.app-config=34-SPRINGCLOUD-SERVICE-PORTAL,34-SPRINGCLOUD-SERVICE-HYSTRIX
turbine.cluster-name-expression="default"



```

4、在main方法的入口类上添加注解：

```java
@EnableTurbine //开启turbine
@SpringBootApplication
public class TurbineApplication{
    public static void main(String[] args){
        SpringApplication.run(TurbineApplication.class, args);
    }
}
```

将portal拷贝过来的config、controller不需要就可以进行删除。

拷贝过来的pom.xml当中多余的依赖也可以进行删除（除了spring-cloud-starter-netflix-turbine这一个其余都进行删除）。

接下来为了能对多个使用了hystrix的项目进行监控，我们再准备一份项目，并且里面使用了hystrix：34-springcloud-service-portal-2（可以拷贝一份34-springcloud-service-portal项目）

由于目前测试的是turbine要对两份项目进行聚合，所以此时将portal服务再拷贝一份，叫做34-springcloud-service-portal-2，然后再做测试。(删除该文件夹下的target文件夹以及修改pom.xml中的artifactId、name、description)

打开IDEA-->File----->New------->Module from Existing Sources....------>双击34-springcloud-service-portal-2项目的pom.xml文件------->Next------->Finish

再到parent服务的pom.xml中添加module，如下

```xml
<modules>
	<!--......-->
            <module>../34-springcloud-service-portal-2</module>
</modules>
```

修改portal-2的入口类名称为PortalApplication2以作区分。以及修改portal-2的配置文件中

```properties

server.port=8081


spring.application.name=34-springcloud-service-portal-2


eureka.instance.instance-id=34-springcloud-service-portal-2

```



启动2个服务提供者完成之后，再启动2个消费者。然后启动turbine服务。

最终展示结果需要hystrix dashboard，即还需要启动dashboard服务。

全都启动好了之后就开始测试，测试地址（检查聚合功能有没有实现）：

![image-20210528093015813](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210528093015813.png)

首先进行访问hystrix dashboard服务，(搭建仪表盘的访问地址)即地址：http://localhost:3721/hystrix

通过turbine的聚合到时候在hystrix dashboard仪表盘首页填写的地址即turbine暴露的接口即http://localhost:8080/actuator/hystrix.stream

注意事项在访问turbine的暴露接口之前还需要先进行访问两个hystrix项目的各自任意一个接口，访问了之后，后续才会在hystrix dashboard后台管理页面当中看到数据，否则拿不到数据。所以目前需要把两个hystrix服务访问一下，即portal以及portal-2服务的各自任意一个接口访问一下。 

消费者portal：http://localhost:8080/cloud/goodsLimit 或者 http://localhost:8080/cloud/goodsHystrix 或者 http://localhost:8080/cloud/goodsRibbon 或者 http://localhost:8080/cloud/goodsFeign（该接口虽然没有@HystrixCommand，但是内部是feign声明式接口使用了feign整合hystrix的，即@FeignClient中配置了fallback、fallbackFactory即说明使用有hystrix进行服务降级） （访问的接口地址当中需要带有hystrix的方法，如果没有hystrix则不行，即需要带有hystrix注解的接口，使用hystrix做了服务熔断降级的接口，这样才能够进行聚合，访问其他接口不行，加有@HystrixCommand注解的方法都是具有熔断降级的）

消费者portal-2：http://localhost:8081/cloud/goodsHystrix 或者 http://localhost:8081/cloud/goodsLimit 或者 http://localhost:8081/cloud/goodsRibbon 或者 http://localhost:8081/cloud/goodsFeign

再进行访问消费者即hystrix的两个项目接口地址：http://localhost:8080/hystrix.stream

接着在hystrix dashboard后台管理首页当中输入地址：http://localhost:8080/actuator/hystrix.stream 然后点击Monitor Stream按钮，这表示对portal服务的监控即对8080 服务的监控。

再在hystrix dashboard后台管理首页当中输入地址：http://localhost:8081/actuator/hystrix.stream，然后点击Monitor Stream按钮，这表示是对portal-2服务的监控，即对8081服务的监控。

此时需要做的是对这两个消费者服务进行整体监控，即就是对portal服务以及portal-2服务进行整体监控，那么此时的做法是：

将hystrix dashboard首页(http://localhost:3721/hystrix)当中的地址切换一下即可：

将http://localhost:8080/actuator/hystrix.stream 或者  http://localhost:8081/actuator/hystrix.stream 变成另外一个url，即turbine的地址，即http://localhost:3722/turbine.stream





















