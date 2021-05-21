package com.bjpowernode.springcloud.config;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
* 当前MyRule 规则定义完成之后，即在RestConfig当中的IRule中return new MyRule(); 即可
* */
public class MyRule extends AbstractLoadBalancerRule {
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        //参考RandomRule，该方法当中的内容可以无需实现，实现不实现都可以
    }

    @Override
    public Server choose(Object o) {
        //如果在调用之前打印了如下信息，就说明自定义负载均衡策略就生效了。将portal Application前端项目进行重启
        //接着使用ribbon+restTemplate方式进行调用远程的服务，测试地址：http://localhost:8080/cloud/goods,响应信息如下：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
        //此时goods9100服务被调用，且PortalApplication控制台中输出了如下打印内容，即说明自定义负载均衡策略生效了。走了自己定义的负载均衡策略MyRule
        //goods9100和goods9200服务被随机调用，因为choose()方法参考了RandomRule类当中的choose()方法实现
        //其余的负载均衡策略可以通过阅读源码去看，此为举例说明下如何实现 自定义负载均衡策略
        System.out.println("==============MyRule impl AbstractLoadBalancerRule 自定义的负载均衡策略================");
        /*自己在该方法中进行实现服务的选择
        怎么进行实现，可以从实现了 AbstractLoadBalancerRule抽象类的RoundRobinRule、RandomRule、RetryRule这些类当中进行观察看如何实现，
        比如说看到 RandomRule 随机规则类当中 choose是如何实现的，可以参考一下。
        以下为RandomRule随机规则类进行实现的 choose方法
       public Server choose(Object key) {
        return this.choose(this.getLoadBalancer(), key);
    }
    此时就完成了MyRule的实现负载均衡的算法。参考了RandomRule当中的choose 方法
    现在就使用自己的负载均衡策略进行调用远程服务。即在RestConfig的IRule当中new MyRule()即可
        */
        ILoadBalancer lb = getLoadBalancer();
        if (lb == null) {
            return null;
        } else {
            Server server = null;

            while(server == null) {
                if (Thread.interrupted()) {
                    return null;
                }

                List<Server> upList = lb.getReachableServers();
                List<Server> allList = lb.getAllServers();
                int serverCount = allList.size();
                if (serverCount == 0) {
                    return null;
                }

//                int index = this.chooseRandomInt(serverCount);//从服务当中选一个序号，总服务有三个，选择随机的一个整数，serverCount服务个数，server有三个；chooseRandomInt实现一下即可
                int index = new Random().nextInt(serverCount);//即MyRule实现的随机算法

                server = (Server)upList.get(index);
                if (server == null) {
                    Thread.yield();
                } else {
                    if (server.isAlive()) {
                        return server;
                    }

                    server = null;
                    Thread.yield();
                }
            }

            return server;
        }
    }

    /**
     * 通过服务个数选择随机的一个取值
     * 其实产生一个随机数即可
     * @param serverCount
     * @return
     */
    protected int chooseRandomInt(int serverCount) {
        return ThreadLocalRandom.current().nextInt(serverCount);
    }

    /*
    *
    * 自定义 负载均衡策略 查看 负载均衡策略 的规则，接口是IRule，IRule的实现类为 AbstractLoadBalancerRule，
    * 所以当前 MyRule就需要进行 继承该 抽象类AbstractLoadBalancerRule，来进行覆盖当中的一些方法
    * 当中最核心的方法即 Server choose(Object o)即选择服务的方法。
    * */



}
