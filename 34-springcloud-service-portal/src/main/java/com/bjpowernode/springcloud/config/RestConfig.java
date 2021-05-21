package com.bjpowernode.springcloud.config;

import com.netflix.loadbalancer.ClientConfigEnabledRoundRobinRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    /*
    * 该配置类，即我们需要在Spring容器当中配置的Bean对象
    * 即添加@Configuration 注解
    * 在容器当中要配Bean对象
    * 配置Bean对象使用 @Bean注解 进行配置Bean对象，配置的Bean对象为 RestTemplate
    * 这是一个restful 风格的调用的时候，可以通过这个来去进行调用
    *
    * 这个即相当于在Spring容器当中添加了一个 bean对象
    * 该bean对象即可在 portal当中的controller中 注入进来
    *
    * 添加了@LoadBalanced注解之后再进行重启portal 消费者服务 （结合了Ribbon来实现调用）
    * eureka client依赖主要用于发现服务，而ribbon的@LoadBalanced 主要用于RestTemplate去调用/消费服务，
    * 两者之间应该是存在什么联系，从而添加了Ribbon的@LoadBalanced注解之后，GoodsController中的RestTemplate.getForEntity中的使用服务名称的url就可以使用了
    * 应该是eureka client底层做了封装，所以从而 spring-cloud-starter-netflix-eureka-client该依赖的底层也依赖了ribbon
    *
    * @LoadBalanced 即spring cloud 在底层对 ribbon、eureka、restTemplate等都进行了封装的操作
    * 通过eureka server可以进行注册服务，eureka client进行发现服务
    * restTemplate进行调用远程服务
    * ribbon进行做负载均衡
    * spring cloud帮助开发者进行封装到了这个注解当中@LoadBalanced
    * 所以该注解是在org.springframework.cloud.client.loadbalancer.LoadBalanced 在spring cloud jar包下面
    * 这个注解即将 ribbon、eureka、restTemplate进行了封装，封装到了@LoadBalanced注解当中来
    * 所以使得有@LoadBalanced 注解的restTemplate可以直接调用远程服务，且实现了负载均衡；
    * 在ribbon进行负载均衡时首先需要找到服务，goods服务通过集群的方式进行部署了三台服务，
    * 这个时候就可以通过eureka去找到这三台服务的注册服务信息表，然后找到服务的ribbon再去进行调用
    * */
    @LoadBalanced //此处即相当于将Ribbon加入进来了，使用Ribbon来进行负载均衡的调用，这样的话就可以调通，不然的话访问 http://localhost:8080/cloud/goods 就会出现 UNKNOWN HOST EXCEPTION,找不到主机异常 这种问题
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();//返回直接生成对象即可，到时候消费者项目即portal的controller通过restTemplate去进行调用服务方goods当中的controller即可
    }


    /*
    如果要切换负载均衡策略，在spring容器当中则配置一个负载均衡策略的Bean即可
    * 上面的@LoadBalanced注解底层默认使用的负载均衡算法是ZoneAwareLoadBalancer类进行实现的，
    * 现在需要切换负载均衡算法，那么就需要覆盖掉@LoadBalanced注解中ZoneAvoidanceRule类所实现的负载均衡算法。
    * 即定义一个Bean，在这个Bean当中进行新生成一个负载均衡算法的实现方式实例。RoundRobinRule该实现方式即轮询。
    * 只需要在配置类当中添加一个Bean即可，该Bean为IRule，RoundRobinRule为IRule的实现类。所以下面可以这样写。
    * 如果需要切换成另外的负载均衡实现方式，则返回生成其他的新的负载均衡实现方式类即可（除了抽象类外）。
    *
    * 配置了如下策略之后，可以将PortalApplication关闭重新debug启动，ILoadBalancer入口Server chooseServer(Object var1);中去各个实现类当中的chooseServer打断点
    * 在ZoneAwareLoadBalancer的chooseServer中打断点以及BaseLoadBalancer中的chooseServer打断点，NoOpLoadBalancer中的chooseServer打断点。
    * 重新启动portalApplication debug启动。
    * 测试负载均衡访问地址：http://localhost:8080/cloud/goods，看一下是否切换了负载均衡策略，即切换了负载均衡实现方式为RoundRobinRule 轮询的方式。
    * 此时发现进入到断点  ZoneAwareLoadBalancer当中来了。
    public Server chooseServer(Object key) {
        if (ENABLED.get() && this.getLoadBalancerStats().getAvailableZones().size() > 1) {
        继续向下执行到了ZoneAwareLoadBalancer当中的else块中打印日志以及返回chooseServer(key)
        else {
            logger.debug("Zone aware logic disabled or there is only one zone");
            return super.chooseServer(key);
        }
        再继续向下执行，进入到BaseLoadBalancer当中的代码行 判断counter计数器是否为空，
        public Server chooseServer(Object key) {
        if (this.counter == null) {
        再接着继续向下执行到了BaseLoadBalancer中代码行
        this.counter.increment();
        继续向下执行，看到BaseLoadBalancer该代码行时，断点可以看到该rule的取值为 rule:RoundRobinRule@8508
                if (this.rule == null) {
        之前在没有切换负载均衡方式的时候，该rule的取值为ZoneAvoidanceRule@8523，即之前默认的负载均衡实现方式为ZoneAvoidanceRule类去进行实现的。
        而现在切换了负载均衡方式之后，该rule的取值变为了RoundRobinRule 轮询方式
        通过断点查看的方式发现负载均衡的实现方式已经切换过来了。（这是通过源码的方式进行的验证是否切换了负载均衡实现方式） 也可以通过测试消费地址看控制台打印信息来观察是否为轮询的方式
        以上即为ribbon切换负载均衡策略的方式。

        通过看控制台的方式即为，第一次刷新地址走的的是goods9200，第二次走的是goods9100服务，第三次走的是goods9200，第四次走的还是9200，第五次是9100
    * */
    @Bean
    public IRule iRule(){
        //采用轮询方式进行负载均衡，即通过重新定义一个Bean的方式，当中返回的负载均衡实现方式不再是ZoneAwareLoadBalancer该实现方式了，而是RoundRobinRule轮询的方式进行实现负载均衡
         return new MyRule();//此处采用自定义的负载均衡策略，参考了RandomRule的实现

//        return new RoundRobinRule();
//        return new ClientConfigEnabledRoundRobinRule();
//        ...

        /*
        这就是负载均衡切换的实现方式，即定义重新定义IRule Bean
        public abstract class AbstractLoadBalancerRule implements IRule, IClientConfigAware { 抽象类无法new 不能返回该种负载均衡实现方式
        public class AvailabilityFilteringRule extends PredicateBasedRule {
        public class BestAvailableRule extends ClientConfigEnabledRoundRobinRule {
        public class ClientConfigEnabledRoundRobinRule extends AbstractLoadBalancerRule {
        public abstract class PredicateBasedRule extends ClientConfigEnabledRoundRobinRule { 理由同上，抽象类
        public class RandomRule extends AbstractLoadBalancerRule {
        public class ResponseTimeWeightedRule extends RoundRobinRule { 该类过期了
        public class RetryRule extends AbstractLoadBalancerRule {
        public class RoundRobinRule extends AbstractLoadBalancerRule {
        public class WeightedResponseTimeRule extends RoundRobinRule {
        public class ZoneAvoidanceRule extends PredicateBasedRule {
        * */

    }
}
