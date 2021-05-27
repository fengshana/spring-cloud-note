package com.bjpowernode.springcloud.service;

import com.bjpowernode.springcloud.model.ResultObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/*
下面学习的是通过feign和hystrix的整合获取异常信息
即在声明式feign的接口GoodsRemoteClient上的注解@FeignClient中配置项加一个fallbackFactory即fallback工厂。
即不再是使用fallback该配置项了。
而该工厂fallbackFactory配置项的取值为一个类。在该类当中去进行实现获取远程服务的异常信息。
该类即定义名称为 GoodsRemoteClientFallBackFactory.class 放在commons服务相同的package下，即service下
================================================================================================
下面学习的是feign和hystrix的整合实现服务降级
feign的声明即GoodsRemoteClient，GoodsRemoteClient为feign声明式调用远程服务的接口
在@FeignClient注解当中有一个参数即fallback；Class<?> fallback() default void.class;
点进去该注解即可看到，即可以通过注解@FeignClient的该配置项Class<?> fallback() default void.class;来指定服务降级
也就是说不再在portal服务消费者当中的controller当中的接口方法@HystrixCommand注解当中的配置项去进行指定fallbackMethod="fallback"方法这样子的一个方式了；
而是在feign声明式的接口当中去进行生命，即通过@FeignClient注解的配置项来实现。
而@FeignClient注解的该配置项Class<?> fallback() default void.class; 是需要一个类，指定一个类，专门作为GoodsRemoteClient的服务降级类。
即现在创建一个叫做GoodsRemoteClientFallBack.java的类即可
也就是当服务降级的时候，GoodsRemoteClient接口的抽象方法到时候走服务降级的时候，让hystrix直接走@FeignClient注解当中的配置项fallback -> GoodsRemoteClientFallBack类即可
GoodsRemoteClientFallBack即服务降级类
服务降级类的类名可以自己进行指定。
在同package即service下建立一个GoodsRemoteClientFallBack.java类
========================================================================================
梳理一下现在学到注解：@EnableEurekaServer、@EnableEurekaClient、@LoadBalanced、@FeignClient("具体的对应的服务名称spring.application.name不区分大小写")、@EnableFeignClients

* 通过添加@FeignClient注解，那么此时GoodsRemoteClient即为Feign的一个客户端，然后该类当中的goods()即为服务提供者的接口，
* 然后它的实现即为goods服务当中的controller当中的goods()方法，它的实现在服务提供者当中的controller进行实现，把服务提供者的方法照抄一份即可
* 通过@RequestMapping("/service/goods")进行映射到服务提供者当中去，到时候调用提供者即可，通过@RequestMapping进行映射过去
* @FeignClient("服务的名称") 该服务的名称即为服务提供者的服务名称，即在服务提供者goods的application.properties文件中配置的spring.application.name中配置的内容
* 即 34-springcloud-service-goods
* 在@FeignClient后面的小括号当中进行指定一下，该接口到时候对应的服务名称是哪一个
* 这个服务名称可以大小写都可以，到时候会和服务名称为34-springcloud-service-goods当中的接口进行对应
* 此时就相当于接口已经准备好了
* 接下来的问题即在于在 调用者那边去调用该接口，即在portal消费者服务端进行调用远程服务也就是GoodsRemoteClient当中的接口。
* 接下来去往portal服务的PortalApplication当中进行配置。
*
* */
//@FeignClient("34-springcloud-service-goods")
//@FeignClient("34-SPRINGCLOUD-SERVICE-GOODS")
//@FeignClient(value = "34-SPRINGCLOUD-SERVICE-GOODS", fallback = GoodsRemoteClientFallBack.class)
@FeignClient(value = "34-SPRINGCLOUD-SERVICE-GOODS", fallbackFactory = GoodsRemoteClientFallBackFactory.class)
public interface GoodsRemoteClient {

    /*
    * 接口，产品远程客户端
    * 在Goods服务当中的GoodsController中，GoodsController相当于是GoodsRemoteClient该接口层的实现
    * GoodsController相当于接口实现
    * 即goods服务是服务提供者，而GoodsController是消费者实现，在GoodsController当中存在有goods()方法
    * 将该方法放到该GoodsRemoteClient接口层当中来。相当于是GoodsController实现了GoodsRemoteClient接口。
    * */

    /**
     * 声明一个feign的接口，它的实现是在服务提供者goods9100和goods9200的controller实现（即GoodsController）
     * 它的实现是服务提供者的controller，它的实现并不是service impl这种,即不是GoodsServiceImpl文件。GoodsServiceImpl是底层连接数据库进行使用的，GoodsServiceImpl实现的接口是GoodsService
     * GoodsController实现的是GoodsRemoteClient, 服务提供者的controller才是GoodsRemoteClient的实现
     * 查询所有商品
     * 该Model没有使用到，进行删除，同时也在goods9100、goods9200当中的GoodsController当中的goods方法中进行删除Model model参数
     * 因为GoodsController是GoodsRemoteClient的实现
     * GoodsRemoteClient和GoodsController当中的方法保持统一
     * 这就是 声明的一个接口，规范定义的
     *
     * 当前@RequestMapping("/service/goods")没有进行识别，需要添加一些jar包；
     *
     * 使用Feign实现消费者，我们通过下面步骤进行：第一步：创建普通的SpringBoot工程，首先我们来创建一个普通的Spring Boot工程，取名为23-springcloud-service-feign
     * 此时我们是直接通过commons这个项目来当做是 34-springcloud-service-feign的。即就将feign放在普通项目当中即可，放在了commons通用项目当中。
     * 所以第一步即为：把接口放在通用的的接口层、常量类、Model类的项目当中（commons），在这个项目当中定义了一些常量类、Model类、接口，就放在这个项目当中
     * 第二步：添加依赖，要添加的依赖主要是 spring-cloud-starter-netflix-eureka-client 和 spring-cloud-starter-openfeign ，如下
     * 需要添加feign的依赖，此时去往commons服务的pom.xml中进行配置
     * 添加好依赖之后，@RequestMapping就没有报错了。
     *
     * spring-cloud-starter-openfeign当中包含有spring mvc，所以在依赖了openfeign之后，@RequestMapping注解可以找得到没有报错。
     * 查看idea的右侧maven展开commons项目的dependencies，看到openfeign下的底层依赖，当中就包含有spring-cloud-starter，而spring-cloud-starter、spring-web当中就包含有很多spring boot的一些相关依赖
     * org.springframework.cloud:spring-cloud-starter-openfeign:2.1.3.RELEASE
         * org.springframework.cloud:spring-cloud-starter:2.1.3.RELEASE
         * org.springframework.cloud:spring-cloud-openfeign-core:2.1.3.RELEASE
         * org.springframework:spring-web:5.1.10.RELEASE
         * org.springframework.cloud:spring-cloud-commons:2.1.3.RELEASE
         * io.github.openfeign:feign-core:10.2.3
         * io.github.openfeign:feign-slf4j:10.2.3
         * io.github.openfeign:feign-hystrix:10.2.3
     * 因为spring cloud底层是spring boot，而spring boot底层是spring ，所以它们都是spring家族的内容，所以才会依赖spring的相关内容，所以这些东西就都有了
     * spring-web下也依赖了spring 相关的内容，从而spring mvc的相关注解就都有了
     *
     * 第三步：第三步：声明服务 定义一个 GoodsRemoteClient 接口，通过 @FeignClient 注解来指定 服务名称，进而绑定服务，然后再通过 Spring MVC 中提供的注解 来绑定 服务提供者 提供的接口，如下
     * 第四步：添加注解 在项目入口类上添加 @EnableFeignClients 注解表示开启Spring Cloud Feign的支持功能；
     * 第五步：使用Controller中调用服务
     * ...
     *
     * 若此处@RequestMapping("/service/goods")中的url和GoodsController中goods()方法上的RequestMapping中的路径不一致，
     * 则访问http://localhost:8080/cloud/goodsFeign时将报错显示404找不到页面
     * 即映射出错。
     * @return
     */
//    @RequestMapping("/service/goods")
//    @RequestMapping("/service/goodsXXX")
    @RequestMapping("/service/goods")
    public ResultObject goods();
    /*
    * 此处和服务提供者GoodsController中的goods上的@RequestMapping("/service/goods")中填写的值不一样，修改之后parent进行clean以及compile；接着重启PortalApplication
    * 测试访问地址为：http://localhost:8080/service/goodsFeign
    * 将导致映射出错，
    * 即feign调用远程服务失败，通过feign找不到远程服务，
    * 因为远程服务提供者GoodsController当中没有路径为这样的@RequestMapping("/service/goodsXXX") public String goods(){}方法，
    * 所以404找不到服务，找不到服务就相当于是映射找不到，找不到它的实现类，（这里我就不太理解为什么是理解成接口和实现类的关系，可能和底层有关系吧）
    报的错误如下：
    [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is feign.FeignException$NotFound: status 404 reading GoodsRemoteClient#goods()] with root cause
    feign.FeignException$NotFound: status 404 reading GoodsRemoteClient#goods()

    在调用/cloud/goodsFeign时也会走自定义的负载均衡即MyRule，因为feign中也自带有负载均衡，自带有ribbon底下依赖了ribbon，而ribbon进行实现了负载均衡相关算法实现类，即实现了IRule接口。
    从而对IRule重新定义返回的实例对象，也会影响到feign使用的负载均衡算法实现类。
    feign底层对ribbon进行了封装依赖。
    *
    *
    * 理解：
    * 使用feign这种声明式实现远程服务调用，即在commons服务中定义API接口即GoodsRemoteClient，
    * 即该接口即为一个FeignClient，定义FeignClient时还需要备注好该FeignClient是调用的哪一个服务，即服务名称即为远程服务的spring.application.name，也就是远程服务往注册中心注册时所提供的服务名称
    * 即该API接口是服务消费者调用时，所提供的固定的哪一个远程服务，
    * 此时该GoodsRemoteClient该API接口固定给远程服务goods，也就是说该GoodsRemoteClient，该FeignClient工具专门用来调用指定好的spring.application.name 远程服务
    * 即应该可以说是，如果有不同的远程服务，即goods、users、orders等等，那么就将会有不同的FeignClient，即类似GoodsRemoteClient、UsersRemoteClient、OrdersRemoteClient
    * 而对应的远程服务goods、users、orders它们的spring.application.name服务名称都有所不同（不同的远程服务模块往注册中心eureka注册时提供的spring.application.name自然不同），
    * 则比如说goods的spring.application.name=34-springcloud-service-goods，users的spring.application.name=34-springcloud-service-users，orders的spring.application.name=34-springcloud-service-orders
    * 则对应访问这些具体的服务的FeignClient客户端类如GoodsRemoteClient它的FeignClient就需要备注好是34-springcloud-service-goods该服务名称，UsersRemoteClient它的FeignClient就需要备注好是34-springcloud-service-users，OrdersRemoteClient它的FeignClient就需要备注好是34-springcloud-service-orders
    * 即每一个远程服务Controller都会对应的FeignClient，Controller和FeignClient当中的方法需要保持一致的原因在于到时候feign进行访问远程服务的时候
    * 就是通过注解@FeignClient("spring.application.name")远程服务名称 + FeignClient API接口当中的方法的@RequestMapping("/service/goods")注解中的路径，去进行调用远程服务
    * 即比如说调用goods服务当中的goods()方法，该goods()方法的@RequestMapping("/service/goods")路径为/service/goods，该goods服务的服务实例名称为34-springcloud-service-goods,
    * 即可以等于feign在调用的时候，访问的地址为：34-springcloud-service-goods + /service/goods ==> http://34-SPRINGCLOUD-SERVICE-GOODS/service/goods
    * feignClient("spring.application.name")当中填写的值不区分大小写（应该可能是做了统一的转换，比如说统一转换成大写的形式，到时候访问的地址即为↑上述地址）
    * 而这个地址 http://34-SPRINGCLOUD-SERVICE-GOODS/service/goods 也就是ribbon+restTemplate 通过注册中心 发现服务，获取得到服务接口地址从而请求远程服务。
    * 此处为什么填写的是34-springcloud-service-goods，原因在于spring.application.name，该取值即为服务提供在注册中心eureka server注册时所提供的服务名称。
    * 可以去看到goods服务的application.properties中spring.application.name配置时的备注(Application代表spring 服务提供者应用名称)
    * 即通过该服务名称，远程服务向注册中心注册时所提供的的服务名称，通过该服务名称可以找到当时的一些远程服务往注册中心注册的一些信息比如ip、端口，所以说服务名称可以代替ip+端口，
    * 猜测原因就是其内部做了转换，通过服务名称可以找到当时的远程服务向注册中心注册时提供的服务名称、ip、端口等信息。
    * 该内部就是ribbon+restTemplate，ribbon在实现负载均衡调用之前需要eureka client通过服务名称拿取eureka server中相对应服务名称的ip、端口等信息，然后在给到ribbon，这时候ribbon才能够进行负载均衡调用
    * feign的底层封装了的是ribbon+restTemplate+eureka client
    * 可以看到idea右侧maven当中openfeign的底层依赖如下：
    * org.springframework.cloud:spring-cloud-starter-openfeign:2.1.3.RELEASE
            org.springframework.cloud:spring-cloud-starter:2.1.3.RELEASE
            org.springframework.cloud:spring-cloud-openfeign-core:2.1.3.RELEASE
                org.springframework.boot:spring-boot-autoconfigure:2.1.9.RELEASE
                org.springframework.cloud:spring-cloud-netflix-ribbon:2.1.3.RELEASE
                org.springframework.boot:spring-boot-starter-aop:2.1.9.RELEASE
                io.github.openfeign.form:feign-form-spring:3.8.0
            org.springframework:spring-web:5.1.10.RELEASE
            org.springframework.cloud:spring-cloud-commons:2.1.3.RELEASE
            io.github.openfeign:feign-core:10.2.3
            io.github.openfeign:feign-slf4j:10.2.3
            io.github.openfeign:feign-hystrix:10.2.3
     从中可以看到spring-cloud-openfeign-core 该jar包下包含有spring-cloud-netflix-ribbon，restTemplate为spring中包含的，所以spring组件中自然会有restTemplate

     此处也可以理解到为什么PortalApplication服务中需要添加的是@EnableFeignClients 该注解添加了s，因为不止有一个FeignClient，不止有一个远程服务、微服务子项目模块
     所以此处关于feign组件所用到的注解@FeignClient("spring.application.name此为远程服务向注册中心注册时提供的服务名称，届时需要通过该服务名称去eureka server注册中心拿取对应远程服务注册时所提供的ip、端口")
     @EnableFeignClients 放到PortalApplication该类上，因为在该项目当中通过依赖commons项目，从而对应的消费者的GoodsController进行注入@Autowired GoodsRemoteClient，即对应的FeignClient，
     当中应该是有某种机制，只有开启了@EnableFeignClients，其@FeignClient客户端组件才能被调用，才能够进行使用。
     为什么说feign是声明式实现了远程服务调用，是由于注解@EnableFeignClients、@FeignClient的缘故，这两个注解下猜测底下应该是二次封装了ribbon以及restTemplate，即底层还是通过原始的ribbon+restTemplate去进行调用
     因为如果说，使用ribbon+restTemplate的方式的话，还是需要在portal服务的controller中去定义MODULE_SERVICE_URL，因为restTemplate.getForEntity()方法提供远程服务地址。
     而如果说存在有大量的远程服务地址，这个时候如果还是使用ribbon+restTemplate的方式进行调用远程服务的话，就需要定义大量的MODULE_SERVICE_URL，这样并不利于项目后续的可维护性，不方便进行维护。
     就相当于是编程式去进行定义这个远程服务地址。
     即如果说由goods、users、orders...服务，那么就有可能出现如下大量地址
     http://34-springcloud-service-goods/service/goods/query
     http://34-springcloud-service-goods/service/goods/insert
     http://34-springcloud-service-goods/service/goods/update
     http://34-springcloud-service-goods/service/goods/delete
     http://34-springcloud-service-users/service/users/query
     http://34-springcloud-service-users/service/users/insert
     http://34-springcloud-service-users/service/users/update
     http://34-springcloud-service-users/service/users/delete
     http://34-springcloud-service-orders/service/orders/query
     http://34-springcloud-service-orders/service/orders/insert
     http://34-springcloud-service-orders/service/orders/update
     http://34-springcloud-service-orders/service/orders/delete
     .......
     在这当中发现了什么，即需要定义大量的远程服务地址，即使不是上面这种写法，也会存在有在类当中定义好服务实例名称（spring.application.name）以及拼接接口名称的做法得到相应的远程服务地址
     因为restTemplate.getForEntity()方法中有参数为String serviceUrl，该参数。所以就会导致在Portal服务的controller中会有大量定义远程服务接口地址的变量。这样非常不好。

     所以此时通过feign了解什么是声明式调用远程服务，声明式调用远程服务，只需要提供服务实例名称和远程服务接口路径即@RequestMapping小括号中填写的内容即可
     虽然这个FeignClient当中也需要提供spring.application.name和@RequestMapping小括号中的接口路径，和上述中ribbon+RestTemplate 差不太多，都需要提供这两个参数，
     但是他们的不同点就在于，feign底层去进行实现restTemplate.getForEntity(底层去进行拼接这个地址，代码层并不需要我们去做到这一点)
     而ribbon+RestTemplate方式就需要自己手动去写大量的远程服务接口地址。
     所以这就是feign 声明式调用远程服务（即对ribbon+restTemplate的二次封装，feign相较于ribbon+restTemplate来说不仅在代码层面上代码少了很多，而且比较简单清晰一些直观一点）。
     但是就是如果不去理解深刻一点的话，就会感觉feign绕来绕去，又是@FeignClient又是@EnableFeignClients又是@RequestMapping路径映射什么的，就会有点懵。
     总结一下：
     ribbon+eureka client（通过服务实例名称即spring.applicaiton.name）->eureka server（拿取到对应的远程服务在注册中心上注册时提供的ip、端口信息）+restTemplate（拼接spring.application.name所对应的ip:端口+service_url）
     feign=@EnableFeignClients(开启FeignClient服务)+@Autowired 对应的FeignClient+通过对应的FeignClient调用服务（@FeignClient(spring.application.name)+@RequestMapping(service_url)==>spring.application.name:service_url==>底层通过ribbon去让eureka client到eureka server上拿取spring.application.name所对应的远程服务在注册中心上注册的ip、端口号等注册信息==>底层进行拼接 ip:端口/service_url==>通过restTemplate.getForEntity进行调用远程服务接口地址）

    我理解的大概就是这个意思了。
    *
    * 消费者portal项目中PortalApplication中需要开启注解@EnableFeignClients @EnableFeignClients该注解即开启FeignClient客户端服务
    * */

}
