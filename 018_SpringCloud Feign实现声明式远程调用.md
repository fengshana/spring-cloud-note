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





问题：客户端负载均衡能再说说吗？



