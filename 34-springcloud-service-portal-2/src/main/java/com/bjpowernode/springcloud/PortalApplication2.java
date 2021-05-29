package com.bjpowernode.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@SpringCloudApplication
//@EnableCircuitBreaker
/*
在portal服务的pom.xml当中添加好hystrix的起步依赖之后即可以在main方法上进行使用@EnableHystrix注解用以开启熔断器/断路器了
* @EnableHystrix 开启熔断器，即开启Hystrix的服务熔断降级支持
* @Enable....代表开启什么样的功能
* 原来老一点的版本使用注解@EnableCircuitBreaker，使用这个注解也是可以的，@EnableHystrix注解当中的实现上是有添加注解@EnableCircuitBreaker
* 即
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @EnableCircuitBreaker
    public @interface EnableHystrix {
    }
所以说@EnableHystrix注解就可以等价于是@EnableCircuitBreaker注解
还有一个注解即@SpringCloudApplication，可以直接添加该注解@SpringCloudApplication；之前在springboot当中有一个叫做@SpringBootApplication的注解
也存在有一个@SpringCloudApplication的注解,可以点进去看一下该注解当中的实现包含有哪些注解, 包含如下
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableCircuitBreaker
    public @interface SpringCloudApplication {
    }
从这当中可以看到该注解@SpringCloudApplication 注解中也是添加有@EnableCircuitBreaker注解的，包含有服务熔断注解，
以及包含有eureka注册中心即@EnableDiscoveryClient该注解即其实就代表了eureka注册中心
以及还包括有@SpringBootApplication注解
也就是说@SpringCloudApplication包含代替了三个注解分别是@EnableDiscoveryClient、@EnableCircuitBreaker、@SpringBootApplication；
如果使用@SpringCloudApplication注解的话，那么就相当于有了注解@EnableDiscoveryClient、@EnableCircuitBreaker、@SpringBootApplication，
即如果使用@SpringCloudApplication在此PortalApplication main方法上使用的话，
则可以进行省略@SpringBootApplication以及@EnableHystrix以及@EnableEurekaClient这三个注解，只剩下@SpringCloudApplication注解和@EnableFeignClients用于开启feign客户端调用支持注解
@EnableDiscoveryClient是等价于@EnableEurekaClient注解的，而@EnableCircuitBreaker等价于@EnableHystrix注解，所以此处使用@SpringCloudApplication注解也可以，这里只做说明
这是使用hystrix进行服务降级步骤的第二步在入口函数处添加注解@EnableHystrix，接下来是第三步，在调用远程服务的方法上添加注解@HystrixCommand(fallbackMethod="fallback")
即去往portal服务的controller消费者端controller远程调用服务提供者的controller
* */
@EnableHystrix
/*
* 添加注解@EnableFeignClients，该portal服务没有依赖openfeign相关的依赖，但是为什么这个类导入的时候存在，
* 原因在于当前portal服务的pom.xml当中并没有添加openfeign的jar包，
* 但是当前portal项目的pom.xml中进行依赖了commons项目，commons项目当中进行依赖了这个jar包也就是spring-cloud-starter-openfeign，
* 从而当前项目portal服务也可以拿到openfeign的相关注解相关类相关内容，所以此时可以进行导入@EnableFeignClients该注解
* 因为当前portal项目当中进行依赖了commons项目即portal服务的pom.xml如下
* <!--消费者的controller也需要需要公共项目 commons的Constants等、model等-->
        <!--依赖commons的常量类 model类
        依赖统一的springcloud-service-commons项目
        然后进行import Changes
        -->
        <dependency>
            <groupId>com.bjpowernode.springcloud</groupId>
            <artifactId>34-springcloud-service-commons</artifactId>
            <version>1.0.0</version>
        </dependency>
        因为当前portal项目依赖了commons项目，而commons项目依赖了spring-cloud-starter-openfeign的jar包
        所以当前在portal项目当中也可以拿到openfeign的相关jar包相关内容
        添加@EnableFeignClients开启一下feign的客户端调用支持，它的一个客户端调用，远程调用的支持进行开启

        完成上述操作之后，然后接着就在portal服务当中的controller中进行调用服务，此时去往portal服务的controller具体调用
* */
@EnableFeignClients
@EnableEurekaClient //第三个步骤，添加@EnableEurekaClient注解，该注解的作用在于 激活 Eureka中的EnableEurekaClient功能， （开启eureka客户端服务的支持）
@SpringBootApplication
public class PortalApplication2 {

    //在Spring boot 程序的入口函数处，通过添加@EnableEurekaClient注解来表明自己是一个eureka客户端，让我的服务提供者可以连接eureka server 注册中心；将服务注册上去
    //刚才在没有添加@EnableEurekaClient注解的情况下，也将服务进行注册到了eureka server注册中心服务端上去了
    //标准步骤当中是需要进行添加注解@EnableEurekaClient的，虽然在页面 http://localhost:8761 首页中看得到有记录，但是一定要加注解进行激活
    //如果不添加的话后续可能会出现某些问题；
    // 消费提供者服务项目goods和消费者服务项目portal都是eureka client 都需要进行添加 @EnableEurekaClient注解,进行表示eureka client客户端激活
    //在http://127.0.0.1:8761/ 的首页HOME中就可以看到Application有两条记录，分别是34-SPRINGCLOUD-SERVICE-GOODS和34-SPRINGCLOUD-SERVICE-PORTAL
    //这就是将eureka client将服务注册到 eureka server上的步骤，通过这些步骤即可

    /**
     *  从goods 服务当中粘贴GoodsApplication、controller过来
     *  因为消费者项目也需要controller
     *  粘贴过来之后进行改造
     *  1. 首先main方法进行修改类名称为 PortalApplication 该类当中的内容也是和GoodsApplication中一致也是标准的 @SpringBootApplication 程序代码
     *  2. 然后就是controller
     *
     */
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication2.class, args);
        /**
         Tomcat started on port(s): 8080 (http) with context path ''
         2021-05-19 15:12:16.866  INFO 8472 --- [           main] c.b.springcloud.PortalApplication        : Started PortalApplication in 1.745 seconds (JVM running for 3.283)

         控制台出现该描述即说明项目运行起来了
         */
    }
}
