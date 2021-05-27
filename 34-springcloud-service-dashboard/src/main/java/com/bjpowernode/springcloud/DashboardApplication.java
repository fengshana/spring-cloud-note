package com.bjpowernode.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * @EnableHystrixDashboard即开启 hystrix仪表盘功能
 * 该项目dashboard当中其他的东西用不着即，将config、controller的package删除即可
 * 单独就留下一个main方法即可
 *
 * 使用仪表盘功能
 * 1. 搭建springboot工程
 * 2. 添加有spring-cloud-starter-netflix-hystrix-dashboard依赖
 * 3. 在入口类上添加@EnableHystrixDashboard以表示开启hystrix 仪表盘功能。
 * 4. 在application.properties中修改server.port=3721即可
 * 5. 修改dashboard服务当中的PortalApplication为DashboardApplication
 * 6. 需要将该dashboard项目添加到parent父项目的pom.xml中的modules 聚合一下即：
 * <modules>
 *      <! --.......-->
 *      <module>../34-springcloud-service-dashboard</module>
 * </modules>
 * 如果parent父项目的pom.xml不加上的话那么dashboard将自己为一个root项目，到时候将报错找不到主类
 * 接着就可以运行dashboard项目了
 * 启动好之后访问地址：http://localhost:3721/hystrix
 * 该为仪表盘地址，打开的页面为hystrix的监控后台
 * 该监控后台首页中的
 * 【https://hostname:port/turbine/turbine.stream        】该文本框当中输入要监控的服务地址
 * Delay:【2000      】ms 该文本框中输入轮询监控的延迟时间，默认为2000ms
 * Title:【Example Hystrix App       】该文本框中输入仪表盘上的标题，默认使用URL
 *
 *
 * 然后即对某个服务进行监控，在hystrix dashboard服务搭建好之后还需要对服务进行监控则此时
 * dashboard服务搭建好了之后，可以认为dashboard服务是一个服务端，到时候客户端即portal项目服务，即使用到了hystrix的项目
 * 这些使用到了hystrix项目的运行情况如何，即还需要在使用到了hystrix的项目当中进行配置一下，让dashboard服务知道
 * 那么到时候dashboard服务的后台就将会展示 使用到了hystrix的项目的hystrix运行情况
 * portal服务当中使用到了hystrix，然后该portal服务当中的hystrix运行的情况如何，它运行的各个指标是否正常，在portal服务当中配置一下，就相当于告诉一下dashboard服务一下，
 * 告诉dashboard服务，到时候访问dashboard的后台管理地址时，就可以进行展示portal服务当中hystrix的各项指标是否正常等运行情况
 * 所以下面需要做的事情就是将用到了hystrix功能的项目当中集成配置一下即可使用
 * 目前portal服务当中已经添加好了hystrix的依赖。
 */
@EnableHystrixDashboard
@SpringBootApplication
public class DashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }
}
