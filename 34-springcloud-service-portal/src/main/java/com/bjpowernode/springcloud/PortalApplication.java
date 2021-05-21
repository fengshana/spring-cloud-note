package com.bjpowernode.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient //第三个步骤，添加@EnableEurekaClient注解，该注解的作用在于 激活 Eureka中的EnableEurekaClient功能，
@SpringBootApplication
public class PortalApplication {

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
        SpringApplication.run(PortalApplication.class, args);
        /**
         Tomcat started on port(s): 8080 (http) with context path ''
         2021-05-19 15:12:16.866  INFO 8472 --- [           main] c.b.springcloud.PortalApplication        : Started PortalApplication in 1.745 seconds (JVM running for 3.283)

         控制台出现该描述即说明项目运行起来了
         */
    }
}
