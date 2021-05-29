package com.bjpowernode.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.turbine.EnableTurbine;


/**
 * 使用turbine一共三个步骤：
 * 1、添加依赖spring-cloud-starter-netflix-turbine
 * 2、application.properties配置turbine相关
 * 3、入口类添加@EnableTurbine注解开启turbine功能
 */
@EnableTurbine //开启turbine对hystrix聚合汇总支持
@SpringBootApplication
public class TurbineApplication {
    public static void main(String[] args) {
        SpringApplication.run(TurbineApplication.class, args);
    }
}
