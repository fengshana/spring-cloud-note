#### Spring Cloud Feign 实现声明式远程调用

##### 5、Spring Cloud Feign

一个新的组件叫做 Feign。Feign又是Spring Cloud进行封装的一个组件，以及该Feign组件又是Netflix 公司的一个开源组件，所以Spring Cloud官方版本可以称作叫Spring Cloud Netflix。

首先了解一下 Feign是什么东西？

**Feign是什么？**

Feign 是 Netflix 公司开发的 一个声明式的 REST 调用客户端；（可以理解为是一个调用服务的工具，调用远程的RESTful 风格的http接口的一个组件，可以这么认为的一个组件。用白话理解）

调用远程的http接口，这个接口是restful的，调用另外一个controller，调用另外的controller的一个组件。

关于调用前面学习到了，调用有很多方式，当下做一个梳理：

* 在原来java开发时是使用的HTTPClient进行调用，httpClient是apache下的组件。
* jdk中自带有 HttpUrlConnection
* Spring当中带有 RestTemplate，也是调用
* 比如说还有 OkHttp也可以用来进行调用服务，这是android开发的时候，里面经常会使用到OKHttp，android调用后端服务接口很多时候是使用的OKHttp。
* Feign，其实也是一种调用方式，是最优雅的一种调用方式，是Netflix公司的，实现非常优雅。

可以理解为以上都是通过一种工具来实现调用服务。

可以有很多方式去进行调用服务。

接下来要讲的就是Feign（Netflix），其实也就是调用远程的一个Controller，看怎么去调用，通过Feign来进行调用远程服务。

Feign这个组件是Netflix公司的，Spring Cloud只是对这个Feign组件进行了封装。封装了之后，Feign这个封装后的组件对Ribbon组件的负载均衡进行了简化。 

Spring Cloud Feign 对比 Ribbon 负载均衡进行了简化，在其 基础上进行了进一步的封装，在配置上 大大简化了开发工作，它是一种 声明式的调用方式，它的使用方式是 定义一个接口，然后在接口上添加注解（代码层上非常优雅），使其支持了Spring MVC标准注解和 HttpMessageConverters（可以实现消息的转型，数据的转型，即在http当中实现了HttpMessageConverters的转化，类型转化），Feign可以与 Eureka和Ribbon组合使用以支持负载均衡。

也就是相当于Feign里面对Ribbon组件和Eureka组件进行了封装，Eureka包括RestTemplate，那么当前使用了Feign的话，RestTemplate就不再需要出场了，它们两个的作用是一样的。

Feign将Eureka和Ribbon进一步的封装了，让开发者进行使用的时候更加方便。

##### Feign能干什么？

Feign 旨在简化 微服务消费方（调用者、客户端）代码的开发，

前面在使用 Ribbon+RestTemplate 进行服务调用时，利用RestTemplate对http请求的封装处理，形成了一套 模板化的调用方式，但是在实际开发中，由于 服务提供者提供的接口非常多，一个接口也可能会被多处调用，Feign在Ribbon+RestTemplate的基础上做了进一步封装，在Feign封装之后，我们只需要 创建一个接口并使用注解的方式来配置，即可完成对 服务提供方的接口绑定（接口绑定之后可以直接用这个接口调用），简化了使用 Ribbon+RestTemplate的调用，自动封装服务调用客户端，减少代码开发量。

消费方即调用者，即客户端。

之前是使用Ribbon+RestTemplate的模式进行调用服务。 

接下来看 Feign是怎么实现，怎么使用的。

通过Feign调用，可以想象一下，它相当于原来在开发dubbo的时候，有消费者、提供者、以及接口层，也就是说这个Feign可以认为是搞了一个接口层，就相当于在原来开发dubbo的时候，有一个Service接口，服务提供者就去实现这个接口，这个Feign就相当于这个接口，然后服务提供者去实现这个接口，所以，Feign相当于Dubbo当中的接口层，既然相当于这个接口层的话，那么所以直接在34-springcloud-service-commons当中搞一个接口即可，Feign就可以写在这个项目当中，即可以在这个commons项目当中定义接口，类似dubbo一样。

在commons服务当中再新建一个service包，类似dubbo。

服务提供者goods当中的controller中的方法，即认为服务提供者当中的controller中的接口方法是它的一个实现。这个controller是接口的一个实现，服务提供者goods的接口是Feign接口的实现，也就是在commons的service当中新建一个关于GoodsController的接口。新建一个类叫做 `GoodsRemoteClient`，在commons工程的service包下进行创建。即产品的一个远程客户端接口。



##### 使用 Feign实现消费者

使用 Feign 实现消费者，我们通过下面步骤进行：

1. 第一步：创建普通的 Spring Boot工程

   把接口放在通用的接口层、常量类、model的项目中

2. 第二步：添加依赖

   要添加的依赖主要是 `spring-cloud-starter-openfeign`

   ```xml
   <!--spring-cloud-starter-openfiegn-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-openfeign</artifactId>
   </dependency>
   ```

3. 第三步：声明服务

   定义一个 GoodsRemoteClient 接口，通过 @FeignClient 注解来指定 服务名称，进而绑定服务，然后再通过 Spring MVC 中提供的注解 来绑定 服务提供者 提供的接口，如下

   ```java
   @FeignClient("34-SPRINGCLOUD-SERVICE-GOODS")
   public interface GoodsService(){
       
       @RequestMapping("/service/goods")
       public String goods();
   }
   ```

   这相当于绑定了一个  名叫 34-SPRINGCLOUD-SERVICE-GOODS （这里34-SPRINGCLOUD-SERVICE-GOODS 大小写都可以 34-springcloud-service-goods） 的服务提供者 提供的 `/service/goods` 接口；

4. 第四步：添加注解

   在项目入口类上添加 @EnableFeignClients 注解表示开启Spring Cloud Feign的支持功能；

5. 第五步：使用Controller中调用服务，

   接着来创建一个 Controller 来调用上面的服务，如下：

   ```java
   public class GoodsController{
       
       @Autowired
       private GoodsRemoteClient goodsRemoteClient;
       
       /**
       使用feign进行调用
       */
       @RequestMapping("/cloud/goodsFeign")
       public ResultObject goodsFeign(){
           //调用远程的一个controller，RESTful的调用
           return goodsRemoteClient.goods();
       }
       
   }
   ```

6. 第六步：属性配置

   （在portal服务消费者的application.properties中进行配置）在 application.properties 中指定服务注册中心、端口号等信息，如下：

   ```properties
   server.port=8080
   
   #打开所有的web访问端点
   management.endpoints.web.exposure.include=*
   
   #此实例注册到 eureka服务端的name
   #spring.application.name=34-springcloud-service-feign
   spring.application.name=34-springcloud-service-portal
   
   #不注册自己，我是一个消费者，别人如果不调用我的话，我就不用注册
   eureka.client.register-with-eureka=false
   
   #每间隔2s，向服务端发送一次心跳，证明自己依然“存活”
   eureka.instance.lease-renewal-interval-in-seconds=2
   
   #告诉服务端，如果我10s之内没有给你发送心跳，就代表我故障了，将我踢出掉
   eureka.instance.lease-expiration-duration-in-seconds=10
   
   #告诉服务端，服务实例以ip作为链接，而不是取机器名
   eureka.instance.prefer-ip-address=true
   
   #告诉服务端，服务实例的唯一ID
   #eureka.instance.instance-id=34-springcloud-service-feign
   eureka.instance.instance-id=34-springcloud-service-portal
   
   #eureka注册中心的连接地址
   #eureka.client.service-url.defaultZone=http://eureka8761:8761/eureka
   #eureka.client.service-url.defaultZone=http://eureka8761:8761/eureka,http://eureka8762:8762/eureka,http://eureka8763:8763/eureka
   #此处使用linux环境的高可用eureka server
   #http://192.168.227.128:8761、http://192.168.227.128:8762、http://192.168.227.128:8763
   eureka.client.service-url.defaultZone=http://192.168.227.128:8761/eureka,http://192.168.227.128:8762/eureka,http://192.168.227.128:8763/eureka
   
   ```

7. 测试

   依次启动注册中心、服务提供者和feign实现服务消费者，然后访问如下地址：

   http://localhost:8080/cloud/goodsFeign

   

##### Spring Cloud Ribbon 客户端负载均衡自定义实现	

问题：ribbon怎么使用自定义的负载均衡算法？

如果说ribbon需要实现自定义的负载均衡算法的话，那么即将ILoadBalancer接口进行自己实现以下即可。

如果需要使用自定义的负载均衡，则在portal服务当中的config中写一个关于负载均衡的东西。

##### 使用 Feign 实现消费者的测试

文字描述

负载均衡：

我们知道，Spring Cloud提供了 Ribbon 来实现负载均衡，使用 Ribbon 直接注入一个RestTemplate 对象即可，RestTemplate已经做好了负载均衡的配置；

在Spring Cloud下，使用 Feign 也是直接可以实现负载均衡的，定义一个有 @FeignClient 注解的接口，然后使用 #RequestMapping 注解到方法上 映射远程的REST服务，此方法也是做好 负载均衡配置的；

通过feign 只需要定义服务 绑定接口 且以声明式的方式，优雅而简单的实现了服务调用。

用feign 实现服务调用。

feign主要是提供类似于dubbo的方式，弄一个接口放在commons项目当中，然后就可以实现像dubbo一样的调用，而GoodsRemoteClient接口当中 goods()方法的描述，包括该goods()方法上的注解@RequestMapping("/service/goods")一定要和服务提供者goods9100或者goods9200当中的GoodsController中的goods()方法一样，即GoodsController当中的goods()方法上的@RequestMapping("/service/goods")，GoodsController中的方法即为GoodsRemoteClient接口中的方法实现。

所以也应该有体会，spring cloud当中使用到了各种各样的组件，spring cloud开发就是微服务开发，它里面解决各种各样分布式微服务下的问题，那么这些问题都不是spring cloud自己去进行解决的，而是采用了别人的开源项目，对别人的开源项目进行了进一步封装，自己对别人的开源项目进行了包装，这样一种方式。

很多东西都是别人已经有解决方案了，它将这些解决方案整合起来，让开发人员直接开箱即用即可，spring当中一直都是这样一种理念，包括spring、spring boot都是这样的模式，它们都可以进行整合redis、rabbitmq、mongodb、dubbo等等一些第三方的组件其他公司的开源组件。spring cloud也是一样，整合各种组件。所以后续spring cloud就会讲很多的其他组件，feign、断路器、配置、hystrix、config等等都是它当中的组件。



##### spring cloud Eureka宕机-调用本地缓存+问答交流

问题：客户端负载均衡能再说说吗？

客户端负载均衡原理：

指的是ribbon 这个jar包，通过该jar包来进行实现的负载均衡。（ribbon当中存在有很多个负载均衡算法，去进行调用远程服务有默认的负载均衡算法即ZoneAvoidanceRule该实现类进行实现的负载均衡算法）

传统的负载均衡即说的是Nginx通过分发请求（当中也有不同的负载均衡策略）分发到集群部署的子服务上。

![image-20210522010835354](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210522010835354.png)



即该上图的理解。

分一下步骤来看，现在有后端服务有三台goods service服务，通过集群的方式进行部署的服务。

即服务提供者有三个，都是goods service只不过它们的端口不一样或者ip端口都不一样。通过集群的方式进行部署。集群部署完成之后，会分别将这三个服务提供者goods service启动起来，这三个服务启动起来之后就都会向eureka server注册中心上进行注册服务，因为goods service 的application.properties当中存在有eureka.client.service-url.defaultZone标注有三台eureka server高可用注册中心集群的url，goods service中依赖有eureka client。goods service作为服务提供者以及eureka client肯定是要向eureka server注册中心进行注册服务的。goods service有可能注册到eureka server三台当中的某一台eureka server，由于eureka server高可用注册中心集群之间它们是相互会进行数据的复制的，所以两两相互进行复制，因为eureka server高可用集群之间它们两两相互进行注册。所以三个goods service服务提供者服务在三台机器eureka server注册中心上都会有一个注册信息在上面。

那么接下来就是客户端，客户端启动，该客户端APP也是作为一个服务，消费者服务，也是一个微服务，那么这个时候APP客户端服务启动之后也会向eureka server高可用集群中做一个服务注册，除此之外，APP客户端还会需要调用远程服务，即调用goods service集群部署当中的某一个goods service服务。

所以此时APP客户端需要通过eureka server进行获取远程服务的相关信息，即发现服务就是去eureka 注册中心将集群部署的三个goods service远程服务的注册信息获取得到，即APP客户端从eureka server中获取三个goods service远程服务往eureka server中注册时的服务注册信息，即发现服务。

三个goods service服务的注册信息拿到程序当中来之后，那么此时APP客户端程序的jar包ribbon当中就帮助开发者进行实现了负载均衡，因为它已经知道了有三个goods service服务了，这三个goods service服务的ip、端口已经通过发现服务获取到了，已经知道三个goods service服务的ip、端口，那么这个时候可以在APP客户端通过ribbon基于负载均衡算法随机、轮询等方式，基于这些方式去进行调用远程服务goods service。

也就是在调用之间有一个ribbon jar包，在调用远程服务之前会经过该jar包当中的负载均衡策略的算法，该算法放在了 chooseServer()方法当中了，chooseServer选择服务方法，该方法作为选择服务，会基于其中的规则就看目前配置的是什么规则，配置的是随机RandomRule那么此处将随机选择集群部署的goods service中的一个服务，如果没有配置那么默认的规则是ZoneAvoidanceRule，是一个综合考量的规则即宕机可能性大小以及性能大小方面的考虑去进行选择调用集群部署的goods service当中的某一个，在入口类ILoadBalancerRule当中有方法chooseServer，而chooseServer又有着多个实现方式。

客户端负载均衡即这个负载均衡是在客户端进行实现的，而服务端负载均衡是什么？

即APP端客户端请求先回发送到服务器当中的Nginx或者其他组件，此时服务器再根据某一种规则策略再做转发，转发到集群部署的某一个服务。那么这个叫做服务器端的负载均衡。

在客户端代码当中进行实现的，这个叫做客户端负载均衡。

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.netflix.loadbalancer;

import java.util.List;

public interface ILoadBalancer {
    void addServers(List<Server> var1);

    Server chooseServer(Object var1);

    void markServerDown(Server var1);

    /** @deprecated */
    @Deprecated
    List<Server> getServerList(boolean var1);

    List<Server> getReachableServers();

    List<Server> getAllServers();
}

```

中的方法

```java
Server chooseServer(Object var1);
```

该方法有多个实现方式BaseLoadBalancer、NoOpLoadBalancer、ZoneAwareLoadBalancer三种选择服务的方式；

先经过ZoneAwareLoadBalancer中的super.chooseServer(key)，再走到BaseLoadBalancer中的chooseServer(Object key)，再在BaseLoadBalancer中chooseServer中走到if (this.rule == null) { 此时通过断点就可以发现这是走到哪一个负载均衡策略/规则。





feign负载均衡算法也就是ribbon的算法吗？

feign的负载均衡，其底层就是ribbon的负载均衡。feign的负载均衡底层就是ribbon去进行实现的。所以可以看到当@FeignClient当中的方法上的注解@RequestMapping("路径A")和远程服务当中的方法上的@RequestMapping("路径B")，由于路径不一致映射错误导致报错时发现在报错之前，进行打印了自定义负载均衡策略的一些日志信息，表示feign仍然是进行调用了MyRule自定义的负载均衡策略。而MyRule是实现的ribbon负载均衡的相关接口即AbstractLoadBalancerRule。（AbstractLoadBalancerRule抽象类实现了接口IRule。）进行重新定义了ribbon的默认负载均衡策略。

所以feign底层是使用ribbon进行负载均衡的。所以feign的负载均衡即使用底层的ribbon去进行实现的。



问题：简单理解就是一个在客户端分发请求，一个在服务器端进行分发请求？

是的，一个是借助服务器Nginx、HAProxy、LVS等，它都是首先将请求分发到服务器，然后这个服务器再帮助开发者进行请求的分发。那么这个叫做服务端的负载均衡。

客户端的负载均衡即通过代码去进行实现。



今天主要讲解了Ribbon、Feign声明式的调用方式。

另外还有一个小问题：因为和dubbo进行了一个对比，dubbo服务在注册完成之后，消费者去进行调用服务提供者，然后注册中心宕机了，那么这个消费者还能不能再去进行调用服务提供者？dubbo当中存在有该问题，dubbo当中当服务提供者注册完成之后，然后消费者调用服务提供者，然后此时注册中心宕机了，注册中心不能使用了，那么在dubbo当中，消费者是可以依然调用服务提供者的，虽然注册中心宕机了，但是在dubbo当中服务消费者依然还可以调用服务提供者。

在spring cloud当中也是这样的机制。 

现将两份服务提供者、消费者服务启动起来。以及目前当前在linux上eureka server服务是可用的，此时进行访问一次http://localhost:8080/cloud/goods之后，将linux eureka server服务停掉，再尝试该地址请求测试http://localhost:8080/cloud/goods接口会发现依然会有响应，响应如下：`{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}`

浏览器地址访问并没有发生请求故障，而是可以进行访问。

然后接着三台eureka server不是在linux环境上进行部署的吗，此时将linux环境上的三个eureka server服务停掉。即演示eureka server宕机的情况。

```bash
ps -ef|grep eureka
#找到三个eureka server的进程号

kill  进程号1 进程号2 进程号3
#通过这种方式关闭进程，进程不会立刻停掉，即不会立刻关闭掉，需要等待一段时间

ps -ef|grep eureka
ps -ef|grep eureka
ps -ef|grep eureka
ps -ef|grep eureka
#查看进程信息渐渐少了
```

此时当linux服务上的注册中心关闭服务之后，本地的goods9100和goods9200就出现报错，连接不到注册中心的错误。

因为服务提供者、服务消费者即eureka client和eureka server之间存在有心跳的机制， 然后这个时候通过消费者去调用服务

测试访问地址如下：http://localhost:8080/cloud/goods

也就是说该服务提供者向注册中心注册的注册信息也会帮开发者进行缓存，即缓存服务端列表，（Ctrl+f5强制刷新浏览器页面也有响应）目前eureka server三台高可用集群已经关闭服务了即已经宕机了，注册中心已经不可用了。

所以spring cloud eureka和dubbo一样，也有注册中心宕机之后服务列表缓存的机制。结论就是：当服务提供者启动之后注册到注册中心了，注册好之后然后将服务消费者进行启动，消费者启动之后它就会从注册中心把服务列表缓存到本地，因为消费者调用远程服务地址，而将服务列表缓存到本地之后，就不在需要去到eureka server注册中心上拿取服务列表了。

