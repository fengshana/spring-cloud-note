package com.bjpowernode.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient //第三个步骤，添加@EnableEurekaClient注解，该注解的作用在于 激活 Eureka中的EnableEurekaClient功能
@SpringBootApplication
public class GoodsApplication9200 {
    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication9200.class,args);

        /**
         * 当控制台出现有
         Tomcat started on port(s): 9100 (http) with context path ''
         2021-05-19 13:09:20.840  INFO 23604 --- [           main] c.b.springcloud.GoodsApplication         : Started GoodsApplication in 2.469 seconds (JVM running for 4.836)
         这个即说明运行已经起来了，走完了这个之后，
         接着这个项目依旧是一个springboot项目，那么就按照原来的方式去进行访问接口
         /service/goods 端口是9100
         即：http://localhost:9100/service/goods 或者 http://127.0.0.1:9100/service/goods
         之后出现了 500 报错，报错内容如下：
         Invalid bound statement (not found): com.bjpowernode.springcloud.mapper.GoodsMapper.selectAllGoods
         即sql语句没有找到，那么sql语句没有找到即将配置文件夹resource下的文件*.xml 将其进行编译一下，
        在goods服务当中的pom.xml中添加一个build标签，在build标签下添加如下内容 resources
         <resources>
         < !--  最主要是添加了下面这一段，将xml文件进行编译一下，由于在mapper文件夹下有xml文件；import Changes之后将项目停一下再将项目跑起来
         此处在运行之前mapper.xml当中的运行路径发生错误，进行了更改，更改之后可以进行运行
         -->
         <resource>
         <directory>src/main/java</directory>
         <includes>
         <include>** /*.xml</include>
                </includes>
            </resource>



            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>** /*.*</include>
                </includes>
            </resource>
        </resources>

         然后import Changes
         */
    }


    /**
     java.lang.IllegalArgumentException: XML fragments parsed from previous mappers does not contain value for com.bjpowernode.springcloud.mapper.GoodsMapper.Base_Column_list
     xml文件当中 <include refid="Base_Column_List" /> 中的Base_Column_List 写错了导致没有找到
     解决之后没毛病

     再就是后面解决application.properties当中的serverTimezone问题即可查询出数据
     即当前goods服务是可以运行的，
     查询之后得到如下数据：
     http://127.0.0.1:9100/service/goods
     {"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.00,"store":12},{"id":2,"name":"商品2","price":168.00,"store":1},{"id":3,"name":"商品3","price":25.00,"store":50}]}

     goods服务可以运行之后，接下来进行准备消费者服务的项目
     即调用的项目，消费者它也是一个SpringBoot项目
     */
}