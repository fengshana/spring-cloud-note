package com.bjpowernode.springcloud.controller;

import com.bjpowernode.springcloud.constants.Constant;
import com.bjpowernode.springcloud.model.ResultObject;
import com.bjpowernode.springcloud.service.GoodsRemoteClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.xml.ws.Response;

//@Controller
@RestController
public class GoodsController {


    /*
    feign:
    在第一次调用的时候，通过知道地址GOODS_SERVICE_URL的方式进行restTemplate直连，直接连接远程服务。
    private static final String GOODS_SERVICE_URL = "http://localhost:9100/service/goods";//产品服务的接口地址
    上述通过直连

    //后续，该种方式是通过注册中心的方式发现服务（服务名称）
    private static final String GOODS_SERVICE_URL_02 = "http://34-SPRINGCLOUD-SERVICE-GOODS/service/goods";//产品服务的接口地址

    下面通过第三种方式，即直接声明一下接口GoodsRemoteClient，将该接口注入进来。即
    @Autowired
    private GoodsRemoteClient goodsRemoteClient;

    * */


    //SpringCloud注册中心 Eureka的服务发现
    //首先将该portal 消费者项目进行停掉


    //后续会慢慢变标准；写死产品服务的URL，产品服务的URL即为 http://127.0.0.1:9100/service/goods 或者 http://localhost:9100/service/goods
    //(直连)
    private static final String GOODS_SERVICE_URL = "http://localhost:9100/service/goods";//产品服务的接口地址

    //该ip+端口就不再需要写死而是写服务提供者提供注册在注册中心的服务名称，
    //即将localhost:9100替换成34-SPRINGCLOUD-SERVICE-GOODS 注意是大写字母
    //然后通过该地址，服务消费者再去调用服务提供者的接口即下面的goods()方法当中将restTemplate.getForEntity()中的url GOODS_SERVICE_URL换成 GOODS_SERVICE_URL_02
    //注册中心服务名去进行调用
    private static final String GOODS_SERVICE_URL_02 = "http://34-SPRINGCLOUD-SERVICE-GOODS/service/goods";//产品服务的接口地址

    //当前为消费者，即Controller调用Controller
    //即当前portal为消费者，需要调用服务提供者也就是goods服务当中的controller GoodsController当中的接口
    //goods当中的controller为服务提供方，当前controller为消费方，消费方需要调用服务方的controller
    //即当前controller不再调用service去获取得到数据，当前也没有service

    //feign的远程调用客户端，也就是不再需要 restTemplate进行调用了，直接使用goodsRemoteClient即可
    @Autowired
    private GoodsRemoteClient goodsRemoteClient;//此时goodsRemoteClient就是一个服务了，通过该服务进行调用

    /**
     * 此类可以进行HTTP接口的调用，除此之外还有httpClient 这种apache下的项目也可以进行http接口调用
     * 还有java当中的 java.net包下的java.net.URL的相关类也可以进行 http接口调用
     * 即http接口调用是可以通过很多的方式的
     * spring当中进行提供了工具类 RestTemplate，该模板类可以进行http 接口调用
     *
     * 接下来启动项目并访问，本项目端口为8080（服务提供方goods项目也需要启动服务）
     * 将消费者的服务访问路径修改一下，不要和服务提供方的访问路径一致，做一下区别
     * 消费方的路径为 /cloud/goods
     * 测试访问地址为 http://localhost:8080/cloud/goods 或者 http://127.0.0.1:8080/cloud/goods
     * controller调用controller，消费者是controller，服务提供者也是controller。再次启动PortalApplication 该 main方法
     * 原来的dubbo当中是通过走 dubbo协议，rpc调用，方法调用方法，产生一个代理对象，service是一个代理对象，方法调用方法
     * 而这边是controller调用controller 走的是http协议；这是两者的一个区别
     * 消费者项目的根路径 /cloud/goods
     *
     * 进行访问 http://localhost:8080/cloud/goods 报错500，报错内容如下
     * Type definition error: [simple type, class com.bjpowernode.springcloud.model.ResultObject]; nested exception is com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `com.bjpowernode.springcloud.model.ResultObject` (no Creators, like default construct, exist): cannot deserialize from Object value (no delegate- or property-based Creator) at [Source: (PushbackInputStream); line: 1, column: 2]
     * 对象不能构造，可能是 构造方法的问题
     * 在ResultObject当中新添加一个无参的构造方法，由于自己写了一个有参数的构造方法，那么此时jvm就不再会去提供默认的无参的构造方法了
     * 写好之后重启，浏览器返回如下
     {"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}

     那么以上就是消费者 portal 通过spring提供的restTemplate调用服务提供者goods当中的接口
     即接口http://localhost:8080/cloud/goods 调用接口 http://127.0.0.1:9100/service/goods，得到响应数据返回给了浏览器端

     此时消费者方服务已经到位

     */
    @Autowired
    private RestTemplate restTemplate;//将配置的bean注入进来之后，然后进行controller调用controller，调用远程的controller

    /**
     * 查询所有商品
//     * @param model
     * @return
     */
    @RequestMapping("/cloud/goods")
//    public String goods(Model model){
//    public ResultObject goods(Model model){
        public ResultObject goods(){ //此处的Model model 没有任何作用进行删除
        /*
        controller调用controller 最原始的办法怎么做呢？就是使用Spring当中的Template，一个模板类去进行调用的
        即portal当中的controller调用goods当中的controller
        在portal服务中注入一个bean对象，新建一个package 叫做com.bjpowernode.springcloud.config
        在该包当中新建一个类叫做 RestConfig 即 Rest配置类
        通过restTemplate进行调用远程的 controller
        restTemplate当中有很多的调用方法，选择其中一个方法去进行调用即可
        restTemplate当中有好几种方法请求，比如说get请求、put请求、delete请求、patch请求等等，这是RESTful的一个请求方式
        即http协议当中的那些方式
        当前直接通过restTemplate的getForEntity方法进行调用远程controller即可
        <T> ResponseEntity<T> getForEntity(String var1, Class<T> var2, Object... var3) throws RestClientException;
        第一个值传的是url地址，即远程的接口地址，远程接口地址暂时写死一下，然后通过该地址去调用一下
        第二个值Class<T> var2即远程controller调用之后会返回的对象值，即ResultObject，那么此处这里可以指定一下
        返回的对象叫做 ResponseEntity<T>，此处泛型T为ResultObject对象
       */
        //这就是调用远程controller得到的响应对象，然后该对象当中则存在真实数据
        //此处也直接在前台进行返回
//        ResponseEntity<ResultObject> responseEntity = restTemplate.getForEntity(GOODS_SERVICE_URL, ResultObject.class);

        //SpringCloud注册中心Eureka的服务调用，现在重启一下服务消费者，测试调用服务消费者调用服务提供者的接口，通过服务名称，当前时使用了注册中心的，因为通过服务名称去调用的话，服务名称是服务提供者往注册中心注册服务时所提供的的内容
        //有了注册中心之后，restTemplate.getForEntity中所提供的的url中ip+端口就被替换成了提供服务者往注册中心所提供的的服务名称，而不再是ip+端口了
        //重启服务之后，再次访问 http://localhost:8080/cloud/goods 接口进行测试
        //此时刷新之后页面报错500，取服务时出现了问题：提示(不知道主机异常)I/O error on GET request for "http://34-SPRINGCLOUD-SERVICE-GOODS/service/goods": 34-SPRINGCLOUD-SERVICE-GOODS; nested exception is java.net.UnknownHostException: 34-SPRINGCLOUD-SERVICE-GOODS
        //当前通过restTemplate直接去通过服务名称去进行调用时会抛出UNKNOWNHOSTEXCEPTION，那么这个时候就需要使用到Ribbon来帮eureka client消费者去找服务
        //即Ribbon+eureka client两者结合才能实现服务调用；即在RestConfig类的Bean构造RestTemplate上再加一个注解@LoadBalanced，添加完之后再重启portal消费者服务，再次测试接口http://localhost:8080/cloud/goods
        //返回数据如下：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
        ResponseEntity<ResultObject> responseEntity = restTemplate.getForEntity(GOODS_SERVICE_URL_02, ResultObject.class);
        //那么当前也就实现了基于注册中心这样一种方式的服务发现与服务消费调用，如果RestTemplate的Bean构造中没有添加Ribbon的@LoadBalanced注解，
        //仅仅通过restTemplate或者说是eureka client单个的作用还无法做到restTemplate.getForEntity使用服务名称加接口名称就可以调用服务
        //所以说这个过程是通过 Ribbon的@LoadBalanced和eureka client两者结合起来才能达到 通过服务名称去进行调用服务，而不再是通过ip+端口的方式，而是服务提供者往注册中心注册服务时所提供的服务名称，通过服务名称就可以调用该服务了
        //服务提供者34-SPRINGCLOUD-SERVICE-GOODS想要调用服务消费者34-SPRINGCLOUD-SERVICE-PORTAL,也是可以通过服务名称进行调用的。到时候直接指定 34-SPRINGCLOUD-SERVICE-PORTAL+/接口名称 也是可以进行调用的
        //以上就是通过eureka client+ribbon的一个调用服务的过程
        //猜测：由于eureka client是知道eureka server注册中心的，通过eureka client就可以获取得到往eureka server注册中心注册的一些服务提供者，以及这些服务提供者在服务注册时所提供的服务名称
        //那么这个时候eureka client底层是存在依赖 ribbon的，这个时候ribbon肯定就知道了这个服务名称，且知道这个服务名称所对应的ip+端口，也就是说我的意思是eureka client知道的东西 ribbon也是知道的
        //

        //通过 ResponseEntity 对象中也可以获取得到一些信息，比如说statusCode、statusCodeVallue、Headers、body等等
        //该body即为 调用远程controller返回响应过来的对象 ResultObject
        return responseEntity.getBody();

        //此时通过 restTemplate的一个远程调用即可完成；通过RestTemplate发一个远程调用直接就完成了controller和controller之间的调用
        //该调用过程是一个restful 风格调用，controller调用controller, 返回的数据为JSON
        //拿到响应的数据之后将项目跑起来，调通项目
        //通过restTemplate是可以进行http接口间的调用的，不管是不是在springcloud项目当中，在其他项目当中也是可以使用restTemplate的
    }



    //============================================================================================================


    /**
     * 查询所有商品
//     * @param model
     * @return
     */
    @RequestMapping("/cloud/goodsFeign")
//    public String goods(Model model){
//    public ResultObject goods(Model model){
    public ResultObject goodsFeign(){ //此处的Model model没有作用进行删除
        /*调用远程的一个controller，RESTful调用，此处是通过feign 这种声明式的远程调用，feign只需要声明一个接口即可，然后就可以通过这个接口去调用远程服务提供者提供的服务了。那么开发的时候就非常类似dubbo的接口层一样
        goodsRemoteClient就类似于dubbo里面的接口层一样，dubbo里面的接口一样，那么这样就可以实现调用了。
        此时就可以进行测试了，关闭一下消费者portal服务，服务提供者先不进行关闭，重新启动一下消费者portal Application即可
        当前进行走前端portal项目服务当中的/cloud/goodsFeign接口，即访问 http://localhost:8080/cloud/goodsFeign，通过feign进行调用，它才是通过feign调用
        而上面的 /cloud/goods是通过 ribbon+RestTemplate进行调用的远程服务，此时通过 /cloud/goodsFeign进行访问,即http://localhost:8080/cloud/goodsFeign
        相应内容如下：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
        看到goods9100和goods9200的控制台，可以看到是9100的服务被调用了，再次刷新地址/cloud/goodsFeign尝试访问服务发现调用的是9200，也就是9100和9200所提供的的服务都有被调用到。
        在通过feign进行调用的过程当中是存在有负载均衡调用的算法的。
        即feign当中也自带有负载均衡，因为feign底层进行包装了ribbon，feign将ribbon进行了二次封装。所以在使用声明式调用的时候就已经当中存在有负载均衡了。
        两台goods9100和goods9200服务都有被调用，说明存在有负载均衡。那么这样开发就变得方便了。不再需要使用ribbon和RestTemplate来进行调用远程服务了。
        而是直接使用feign这种声明式调用即可。以上测试通过spring cloud feign实现声明式远程调用就测试完成了。
        */
        return goodsRemoteClient.goods();//通过goodsRemoteClient直接调用goods方法即可
        /*
        * goodsRemoteClient调用goods方法，那么就会走到commons当中的GoodsRemoteClient接口层当中去，而GoodsRemoteClient当中的goods()方法返回ResultObject与当前该消费者需要返回的内容ResultObject类型一致即可
        * 而GoodsRemoteClient接口当中的goods()方法实现，通过该方法上的@RequestMapping("/service/goods")注解，这个实现是在远程服务提供者即goods9100或者是goods9200当中的controller中实现的
        * GoodsRemoteClient接口中的方法由远程提供服务者进行实现
        * 而在portal服务当中，我们只需要调用goodsRemoteClient.goods()接口
        * */
    }






    /**
     * 使用hystrix进行服务降级步骤
     * 1.服务消费者方添加spring-cloud-starter-netflix-hystrix依赖
     * 2.在服务消费者方main中添加注解@EnableHystrix/@EnableCircuitBreaker/@SpringCloudApplicaiton注解
     * 3. 在服务消费者controller调用远程服务的方法上添加注解@HystrixCommand，并配置fallbackMethod方法保持方法签名一致
     *
     * 测试步骤
     * 1. 启动linux虚拟机当中的注册中心 eureka server 三台高可用集群 eureka server（三个节点）。
     * linux: [ps -ef|grep eureka、ll、./eureka_server.sh、ps -ef|grep eureka、ifconfig] | windows: 检查本地mysql服务是否开启[net start mysql]
     * 内网地址依然为192.168.227.128没变，eureka serve地址（检查是否可用）：http://192.168.227.128:8761/、http://192.168.227.128:8762/、http://192.168.227.128:8763/
     * 2. 接着debug启动两个服务提供者goods9100和goods9200，服务提供者提供好之后会自动往注册中心eureka server进行注册，注册中心eureka server目前在linux当中有三个结点，都是已经启动部署好了的，所以这边服务提供者就可以直接去eureka server进行注册服务，届时服务消费者就可以直接去进行调用远程服务了（获取eureka server的远程服务注册信息然后再去调用）
     * 3. 最后debug启动portalApplication服务，portal前端消费者服务。（springcloud当中就是通过一系列的组件进行构成的，spring cloud当中有很多的组件）
     * 4. 上述服务启动完成之后，测试请求访问地址如下：http://localhost:8080/cloud/goodsHystrix 进行调用远程服务
     *
     * 访问地址返回如下响应：{"statusCode":1,"statusMessage":"服务降级了","data":null}
     * 从响应内容上来看可以知道是触发了服务降级，也就是使用到了@HystrixCommand注解当中的服务调用了fallback()方法
     * 由于controller中goodsHystrix()方法是调用了goodsRemoteClient.goods()方法（即使用feign声明式远程调用），该goodsHystrix()方法的降级@HystrixCommand方法为fallback()方法
     * goodsRemoteClient.goods()是通过feign进行声明式调用远程服务。此时看到goodsRemoteClient.goods()在服务提供者goods服务goods9100/goods9200当中Controller中的服务实现，即GoodsController中的goods()方法的实现。
     * 而此时远程服务goods服务的GoodsController中的goods()方法实现中通过goodsService.getAllGoods()进行访问数据库，有可能是查询数据库的时间过长，也就是查询太慢，从而导致就进行服务降级了。
     * hystrix默认的调用远程服务超时时间为1000毫秒，当通过使用feign声明式调用远程服务的这个过程当中兴许是这个过程时长超过了1000毫秒，所以hystrix判定远程服务不可用、异常所以才进行服务降级调用fallbackMethod="fallback"方法
     * 1000毫秒=1秒钟，也就是hystrix默认调用远程服务超时时长为1秒钟，而在通过feign声明式调用远程服务的过程中已经超过了1秒钟，所以此时@HystrixCommand对其进行了服务降级处理，即调用fallback进行返回前端
     * hystrix默认为1秒服务降级。
     * 可以通过debug的方式启动goods9100、goods9200、portal服务，将断点打在 portal中的controller中goodsHystrix()方法的return goodsRemoteClient.goods();该行代码上。
     * 然后再测试地址：http://localhost:8080/cloud/goodsHystrix，此时可以看到断点起作用了，先计算一下goodsRemoteClient.goods()返回了一些什么东西，鼠标选中goodsRemoteClient.goods()之后，点击下方 Debugger一栏中像计算器的图标，即Evaluate Expression（或者可以通过按Ctrl + U 快捷键查看）
     * 在选中代码`goodsRemoteClient.goods()`，然后快捷键Ctrl+U/点击计算器图标之后，会弹出弹窗Evaluate，在弹窗中直接点击按钮 Evaluate即可。
     * 可以看到Result结果如下：
     result = {FeignException$NotFound@10357} Method threw 'feign.FeignException$NotFound' exception.
     status = 404
     content = {byte[137]@10361}
     detailMessage = "status 404 reading GoodsRemoteClient#goods()"
     cause = {FeignException$NotFound@10357} "feign.FeignException$NotFound: status 404 reading GoodsRemoteClient#goods()"
     stackTrace = {StackTraceElement[48]@10363}
     suppressedExceptions = {Collections$UnmodifiableRandomAccessList@9966}  size = 0
     * 在这当中可以看到异常：`Method threw 'feign.FeignException$NotFound' exception.`
     * 发生404错误：`feign.FeignException$NotFound: status 404 reading GoodsRemoteClient#goods()`
     * 所以此时才会触发@HystrixCommand的服务降级调用了fallback()，即调用远程服务中出错也是会进行降级处理。此时可以知道后端方法是有问题的。导致了报错。并不是源于hystrix默认超时一秒钟进行服务降级的原因。
     * 看到status 404 reading GoodsRemoteClient#goods()可以知道是GoodsRemoteClient在进行调用远程服务goods时出现了404，这个时候就去看到goodsRemoteClient.goods当中的实现。
     *  此时的GoodsRemoteClient中接口映射为
     *  @RequestMapping("/service/goodsXXX")
     *     public ResultObject goods();
     * 而远程服务提供者goods9100、goods9200的接口实现为
     * @RequestMapping("/service/goods")
     * public ResultObject goods(){}
     * 也就是说GoodRemoteClient当中的映射RequestMapping路径和远程服务提供者当中的RequestMapping路径不一致，所以才会报错，status 404 reading GoodsRemoteClient#goods() 404 没有找到远程服务接口的错误（GoodsRemoteClient API接口当中的方法需要和服务提供者当中的Controller中的方法实现要保持一致，如果不一致就会导致404，feign底层在调用ribbon+restTemplate的时候路径有问题）
     * 由于GoodsRemoteClient 该API接口是写在commons服务中的，所以此时通过parent进行clean并重新compile，然后再次启动goods9100、goods9200、portal服务。
     * 再次请求地址测试：http://localhost:8080/cloud/goodsHystrix，响应如下：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
     * 可以发现是正常响应了数据。并没有服务降级。
     *
     * 同时从该操作当中可以看到，尽管在goodsRemoteClient在调用远程服务的时候发生了报错，但是返回给前端并没有问题，而是进行了服务降级，给了默认的值
     *
     * portal服务pom.xml中添加热部署插件这样在开发测试的时候会便利一些不用频繁的进行重启服务。goods9100服务和goods9200也添加一下该插件
     * <!--spring boot 开发 自动热部署-->
     <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <optional>true</optional>
    </dependency>
    目前暂时现将消费者服务portal进行重启，（微服务测试、启动服务比较麻烦）

    当前在进行修改了GoodsRemoteClient当中@RequestMapping当中的路径之后，访问服务是不会有报错的，因为此时GoodsRemoteClient调用远程服务是正常的。即如果goodsRemoteClient.goods()该请求在1000毫秒也就是1秒内进行返回了结果，那么就不会触发hystrix的服务降级。
    而goodsRemoteClient.goods()调用远程服务不是在1秒钟进行返回了响应数据，而是超过了1秒钟，那么这个时候Hystrix就会触发服务降级，即调用fallback()
    在代码无问题时，会进行正常的响应。即使得hystrix可以在1秒钟之内拿取得到响应结果，而且是正常的响应，那么这个时候就不会触发hystrix的服务降级。远程服务调用返回的响应并不正常，那么就会触发服务降级。
    goodsRemoteClient.goods()在进行远程服务调用时可以1秒钟之内进行返回结果从而不会触发hystrix的服务降级即调用fallback进行返回给前端。

     *
     * 使用hystrix进行服务降级
     * 即测试消费者地址 http://localhost:8080/cloud/goodsHystrix
     * 通过消费者去请求服务提供者远程的服务，如果服务提供者的服务发生异常或者超时，到时候就会走到@HystrixCommand注解当中配置的fallbackMethod="fallback",fallback()方法当中来
     * 即当前还需要写一个方法叫做 fallback()，该方法的签名和public ResultObject goodsHystrix(){}保持一致，即fallback()方法为 public ResultObject fallback(){}即可
     * 查询所有商品
     * @return
     */
//    @HystrixCommand(fallbackMethod = "fallback")
    /*
    * application.properties配置文件当中进行配置hystrix.command.default.execution.timeout.enabled=true、hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000
    * 就相当于此时在注解当中配置的是一样的意思。
    * 指定执行超时降级服务可用
    * @HystrixProperty(name = "execution.timeout.enabled", value = "true"),
    * 设置hystrix超时时间为5秒
    * @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")
    * 通过application.properties配置文件的方式进行配置这两个配置项取值更加方便。
    * 此时rebuild portal服务，重新启动后测试地址：http://localhost:8080/cloud/goodsHystrix
    * 响应结果如下：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
    * 通过响应结果可以知道没有走服务降级，即通过注解的方式进行配置hystrix的超时时间生效了。
    * 总之修改hystrix的超时时间需要ribbon.ReadTimeout和hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds两者，谁的取值小谁生效
    * */
    @HystrixCommand(fallbackMethod = "fallback",
                    commandProperties = {
                            @HystrixProperty(name = "execution.timeout.enabled", value = "true"),
                            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")
                    },
                    ignoreExceptions = Throwable.class //忽略异常，该异常的类可以写Exception.class或者是RuntimeException.class，或者是其他的一些xxxxException.class类，现在写的是最顶层的异常父类，写了ignoreException之后那么此时远程服务如果抛出了异常那么此时hystrix是不会理会也就是说不会服务降级，而是将异常直接抛给前端浏览器页面当中
    )
    @RequestMapping("/cloud/goodsHystrix")
    public ResultObject goodsHystrix(){ //此处的Model model没有作用进行删除
//        return goodsRemoteClient.goods();
        /*
    做一个测试即使得feign 声明式远程调用服务的时间增加，即超过1秒钟。这个使得时间超过1秒钟的操作可以直接在portal的controller接口层goodsHystrix()方法当中去做，
    也可以去到对应的goods 9100或者是goods9200服务的controller接口goods()方法实现当中去进行修改，使得响应数据时间超过1s，两种方式都可以。
    该修改远程服务调用时间超过1s即直接在当前portal客户端服务的controller中的goodsHystrix()方法当中进行修改即可。
    让线程进行睡眠一会儿，即超过1s即可。
    Hystrix默认是一秒，而此时只需要让线程进行沉睡两秒即可，那么就会触发Hystrix的服务降级
    该让Thread线程休眠两秒的代码在写完之后，将该module进行rebuild，
    即在idea的最上方菜单栏中找到[Build]-->[Build Module '34-springcloud-service-portal']，进行重新编译即可，编译完成之后spring boot devtools自动热部署插件会将portal服务自动热部署
    在portal服务经过spring boot devtools自动热部署插件重新部署之后，再次进行访问测试接口地址：http://localhost:8080/cloud/goodsHystrix，而此时返回的响应结果为：{"statusCode":1,"statusMessage":"服务降级了","data":null}
    此时进行触发了Hystrix的服务降级，即调用fallbackMethod当中配置的方法fallback()提供默认值返回给前端。且再怎么进行反复刷新返回的都是经过服务降级之后的默认数据，
    因为goodsHystrix方法中沉睡了两秒，即超过了hystrix默认的1秒时间，所以hystrix才会触发服务降级。
    且每次刷新地址栏之后返回数据的时间都是2s中后浏览器才得到了默认响应数据。

    这就是hystrix的服务降级的测试，即如果调用远程服务超时了，那么hystrix将会走入到服务降级当中即调用@HystrixCommand中配置的fallbackMethod="fallback"方法。
    以上是一种情况。服务超时会导致服务降级。上面还有就是调用远程服务异常即GoodsRemoteClientAPI接口当中@RequestMapping路径和服务提供者服务的controller实现@RequestMapping的路径不一致，导致feign在调用远程服务的时候找不到服务接口，此时也会造成hystrix的服务降级。

    */
//        1. 服务超时，会降级
//        try {
//            System.out.println("==============goodsHystrix 开始进入睡眠==================");
//            Thread.sleep(2000);//沉睡两秒，休眠两秒，因为hystrix默认是1秒，如果1秒当中调用远程服务没有结果那么则hystrix就会走服务降级即调用fallback();此时通过Thread.sleep沉睡两秒就会触发hystrix的服务降级，即调用fallback方法进行返回给前端默认数据
//            System.out.println("==============goodsHystrix 结束睡眠==================");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        2. （消费者）服务异常，会降级（该portal服务当中的该controller接口层当中的goodsHystrix()方法中出现了异常，那么也是会服务降级的。如果在远程服务调用的时候，该远程服务异常了，会降级。）
//        进行模拟异常
//        String str = null;
//        if(str == null){
//            throw new RuntimeException("服务异常了");
/*            那么这个时候也会走服务降级。进行测试是否响应数据为服务降级之后的默认数据。
              同样还是点击Build当中的Build Module 34-sringcloud-service-portal即可，通过spring boot的自动热部署工具进行自动重启服务即可。
              portal服务启动好之后，测试服务地址：httpL://localhost:8080/cloud/goodsHystrix，返回响应数据：{"statusCode":1,"statusMessage":"服务降级了","data":null}
              从响应数据中可以发现，此操作即消费者服务异常确实是会引起Hystrix进行服务降级，即走入到@HystrixCommand命令中配置的fallbackMethod中的fallback方法中。
*/
//        }

        //测试@HystrixCommand注解的ignoreException = Throwable.class 是否生效 进行重启portal服务；访问测试地址http://localhost:8080/cloud/goodsHystrix 响应数据如下：
        /*
        Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.

Sun May 23 15:04:46 CST 2021
There was an unexpected error (type=Internal Server Error, status=500).
?????
java.lang.RuntimeException: ?????
            at com.bjpowernode.springcloud.controller.GoodsController.goodsHystrix(GoodsController.java:325)
            at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
            at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
            at java.lang.reflect.Method.invoke(Method.java:498)
            at com.netflix.hystrix.contrib.javanica.command.MethodExecutionAction.execute(MethodExecutionAction.java:116)
            at com.netflix.hystrix.contrib.javanica.command.MethodExecutionAction.executeWithArgs(MethodExecutionAction.java:93)
            at com.netflix.hystrix.contrib.javanica.command.MethodExecutionAction.execute(MethodExecutionAction.java:78)
            at com.netflix.hystrix.contrib.javanica.command.GenericCommand$1.execute(GenericCommand.java:48)
            at com.netflix.hystrix.contrib.javanica.command.AbstractHystrixCommand.process(AbstractHystrixCommand.java:145)
            at com.netflix.hystrix.contrib.javanica.command.GenericCommand.run(GenericCommand.java:45)
            at com.netflix.hystrix.HystrixCommand$2.call(HystrixCommand.java:302)
            at com.netflix.hystrix.HystrixCommand$2.call(HystrixCommand.java:298)
            at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:46)
            at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:35)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
            at rx.Observable.unsafeSubscribe(Observable.java:10327)
            at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:51)
            at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:35)
            at rx.Observable.unsafeSubscribe(Observable.java:10327)
            at rx.internal.operators.OnSubscribeDoOnEach.call(OnSubscribeDoOnEach.java:41)
            at rx.internal.operators.OnSubscribeDoOnEach.call(OnSubscribeDoOnEach.java:30)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
            at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
            at rx.Observable.unsafeSubscribe(Observable.java:10327)
            at rx.internal.operators.OperatorSubscribeOn$SubscribeOnSubscriber.call(OperatorSubscribeOn.java:100)
            at com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction$1.call(HystrixContexSchedulerAction.java:56)
            at com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction$1.call(HystrixContexSchedulerAction.java:47)
            at com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction.call(HystrixContexSchedulerAction.java:69)
            at rx.internal.schedulers.ScheduledAction.run(ScheduledAction.java:55)
            at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
            at java.util.concurrent.FutureTask.run(FutureTask.java:266)
            at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
            at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
            at java.lang.Thread.run(Thread.java:748)

            此时可以发现 异常直接抛给前端页面了。即Hystrix忽略异常，从而hystrix就不会走服务降级。然后将异常信息抛给了前台页面。

        现在本地也就是portal服务的controller-goodsHystrix()抛出一个异常，然后portal服务进行重新编译一下。
        重新启动后访问http://localhost:8080/cloud/goodsHystrix
        响应在浏览器页面抛出异常：java.lang.RuntimeException: ?????  at com.bjpowernode.springcloud.controller.GoodsController.goodsHystrix(GoodsController.java:379)
        也就是此时@HystrixCommand注解有了ignoreException之后服务消费端中产生的异常就将直接抛给页面，也就是将服务消费端goodsHystrix()当中的异常进行忽略，不走hystrix的服务降级
        即这个时候不进行服务降级，而是抛给了前端页面，抛给了前端
        */
        String str = null;
        if(str == null){
            throw new RuntimeException("服务异常类");
        }

//        3. 在服务提供者服务，即远程服务发生异常，那么也会触发HystrixCommand的服务熔断降级。此时去远程的服务goods9100/goods9200中的controller goods()实现方法当中进行修改。
//        goods9100和goods9200都需要进行做线程沉睡休眠两秒的操作，因为服务提供者集群部署了两台服务，feign底层会通过ribbon进行负载均衡到这两台中的某一台服务上，所以两台服务goods9100和goods9200都需要进行沉睡休眠2秒的操作。
//        在goods9100和goods9200的远程服务当中进行休眠沉睡了两秒之后，将goods9100和goods9200进行服务重启，接着后续才会通过spring-boot-devtools自动热部署工具进行热部署服务。（即热部署插件依赖加上之后需要立即进行重启，重启完才会生效，之前添加了热部署依赖但是这两个服务并没有进行重启所以现在需要重启令热部署生效，重启完goods9100和goods9200服务之后热部署生效）
//        改了之后可以发现在远程服务当中出现异常或者是超时超过1秒钟，都会引起Hystrix的服务降级处理；
//        此时测试的是 服务提供者中 也就是远程服务中 响应超时，进行了沉睡休眠两秒；远程服务有goods9100和goods9200，它们所提供的的服务当中都做了线程沉睡休眠两秒的操作。即会导致响应超时；
//        测试访问地址：http://localhost:8080/cloud/goodsHystrix；返回响应内容如下：{"statusCode":1,"statusMessage":"服务降级了","data":null}
//        通过响应内容即可知道触发了Hystrix的服务降级，也就是调用了@HystrixCommand注解当中配置的fallbackMethod中fallback方法返回的默认数据
//        即远程服务响应数据超时也是会触发Hystrix的服务降级；消费者portal的controller goodsHystrix()中并没有进行线程休眠两秒。当前该服务当中的线程休眠两秒和抛出运行时异常都注释掉了。
//        为的就是要测试远程服务中的响应超时，远程服务goods9100和goods9200当中都进行开启了线程休眠两秒操作，而Hystrix默认是1秒，那么远程服务显然是超时了。控制单一变量。
//        由于远程服务超时所以会走Hystrix的服务降级。即走到@HystrixCommand注解中配置的fallbackMethod fallback方法中去返回默认的数据。如果将远程服务当中的线程休眠沉睡两秒注释掉去掉，则不会发生Hystrix触发服务降级，而是会正常响应数据
//        远程服务中如果抛异常，那么也是一样的，也会触发Hystrix的服务降级。即返回fallbackMethod fallback方法中默认的数据给前端。服务测试地址：http://localhost:8080/cloud/goodsHystrix，响应数据如下：{"statusCode":1,"statusMessage":"服务降级了","data":null}，调用的是goods9200当中的服务打印了"服务异常了"的日志信息，即说明远程服务内不管是响应超时还是抛出异常都会触发Hystrix的服务降级。


//      进行测试在客户端controller goodsHystrix方法中抛出异常后，则会触发Hystrix的服务降级处理，则走到@HystrixCommand配置的fallbackMethod中的fallback()方法里
//      而fallback()中配置了Throwable throwable方法参数并进行打印了相关信息，此处测试是为了检查fallback当中的Throwable throwable是否拿取到了客户端抛出的异常。
//      将portal服务重新编译后重新自动部署后访问地址：http://localhost:8080/cloud/goodsHystrix,响应如下：{"statusCode":1,"statusMessage":"服务降级了","data":null}
//      因为在消费者portal中goodsHystrix中抛异常RuntimeException，所以服务降级了；服务降级了是可以在fallback()中拿到相关的异常信息的，fallback()打印如下："fallback: 服务异常了。"
//      Throwable是异常类当中的父类，处于最上层的父类
//      我觉得笔记做得太细了。很浪费时间，做笔记自己可以看懂就好了，可是不细怕自己又看不懂卧槽
//      下面这是portal消费者方发生异常时通过fallback拿到了异常信息，下面再进行测试远程服务发生异常，将portal服务goodsHystrix的抛异常代码注释掉，测试远程服务如果发生异常，即在远程服务goods9100、goods9200的goods()方法当中抛出异常即可，再通过fallback的Throwable再去拿取远程服务的异常信息
//        String str = null;
//        if(str == null){
//            throw new RuntimeException("服务异常了。");
//        }



//        System.out.println("==============goodsHystrix feign声明式远程调用服务开始==================");
        return goodsRemoteClient.goods();

    }

    /**
     * 降级方法 fallback；即如果通过goodsRemoteClient.goods()发生了调用远程服务异常、响应超时等那么就会调用@HystrixCommand注解中配置好的降级方法 fallbackMethod = "fallback"
     * 到时候给前端返回一个默认结果即可即：return new ResultObject(Constant.ONE, "服务降级了");
     * 此时就实现了不会将goodsRemoteClient.goods()调用远程服务当中出现的异常、响应超时等信息发送给前台，而是会给前台返回结果为默认的值，即降级方法当中返回的内容
     * 此时就编写完成可以进行测试hystrix 服务降级
     *
     * 该方法对应着上述goodsHystrix()方法上的注解@HystrixCommand中的配置项 fallbackMethod = "fallback"，两者存在映射
     * 当前该fallback方法的访问类型、返回类型（签名）和上述方法goodsHystrix()进行保持一致。
     * 相当于如果goodsHystrix()方法当中，通过goodsRemoteClient feign声明式调用远程服务.goods()方法超时了，或者异常了
     * 那么此时则goodsHystrix()方法当中的内容即不再执行了也就是 return goodsRemoteClient.goods()不再去进行执行
     * 然后就会去调用@HystrixCommand注解当中配置好的fallbackMethod = "fallback"，fallback()方法。
     * 那么该fallback()方法和上述的goodsHystrix()的方法签名是一样一致的。
     * 因为在调用fallback()方法的时候是需要达到和goodsHystrix()方法一样的目的。只是fallback()方法返回的数据可能是一个默认的值。
     * 即在fallback()方法当中return 一个默认值，该默认值为ResultObject的实例对象。
     * 而在goodsHystrix()方法当中返回的取值是真实的有数据的，是从缓存或者是从数据库当中拿取到的记录行。而fallback()仅仅只是一个默认值而已。
     * 以上就是使用hystrix 默认的实现方式。
     *
     * 降级方法，也就是备用的方法。当远程服务不可用、远程服务超时、远程服务异常的时候，服务降级被触发，到时候就会直接调用fallback()方法返回给前端默认的数据。
     *
     *
     * @return
     */
//    public ResultObject fallback(){
    public ResultObject fallback(Throwable throwable){ //在fallback()方法中添加方法参数Throwable throwable这样就可拿到异常的相关信息，即异常的类型等
        /*
        Spring Cloud hystrix异常处理，在@HystrixCommand的配置项fallbackMethod="fallback"，即fallback()方法中添加方法参数即Throwable throwable
        * 这里可以对异常信息做出一些处理
        * 以下就是对异常信息的打印，即获取得到异常，并可以针对该异常做相关处理。
        * 即如果远程服务抛出异常或者是消费者自己抛出了异常，都是可以拿到异常的相关信息的。
        * 当下进行测试比如说在客户端portal controller 的goodsHystrix()方法当中抛出运行时异常，也就是第二点取消注释
        * */
        throwable.printStackTrace();//对异常的打印 即打印如下
        /*
        java.lang.RuntimeException: 服务异常了。
                at com.bjpowernode.springcloud.controller.GoodsController.goodsHystrix(GoodsController.java:340)
                at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
                at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
                at java.lang.reflect.Method.invoke(Method.java:498)
                at com.netflix.hystrix.contrib.javanica.command.MethodExecutionAction.execute(MethodExecutionAction.java:116)
                at com.netflix.hystrix.contrib.javanica.command.MethodExecutionAction.executeWithArgs(MethodExecutionAction.java:93)
                at com.netflix.hystrix.contrib.javanica.command.MethodExecutionAction.execute(MethodExecutionAction.java:78)
                at com.netflix.hystrix.contrib.javanica.command.GenericCommand$1.execute(GenericCommand.java:48)
                at com.netflix.hystrix.contrib.javanica.command.AbstractHystrixCommand.process(AbstractHystrixCommand.java:145)
                at com.netflix.hystrix.contrib.javanica.command.GenericCommand.run(GenericCommand.java:45)
                at com.netflix.hystrix.HystrixCommand$2.call(HystrixCommand.java:302)
                at com.netflix.hystrix.HystrixCommand$2.call(HystrixCommand.java:298)
                at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:46)
                at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:35)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
                at rx.Observable.unsafeSubscribe(Observable.java:10327)
                at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:51)
                at rx.internal.operators.OnSubscribeDefer.call(OnSubscribeDefer.java:35)
                at rx.Observable.unsafeSubscribe(Observable.java:10327)
                at rx.internal.operators.OnSubscribeDoOnEach.call(OnSubscribeDoOnEach.java:41)
                at rx.internal.operators.OnSubscribeDoOnEach.call(OnSubscribeDoOnEach.java:30)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
                at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
                at rx.Observable.unsafeSubscribe(Observable.java:10327)
                at rx.internal.operators.OperatorSubscribeOn$SubscribeOnSubscriber.call(OperatorSubscribeOn.java:100)
                at com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction$1.call(HystrixContexSchedulerAction.java:56)
                at com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction$1.call(HystrixContexSchedulerAction.java:47)
                at com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction.call(HystrixContexSchedulerAction.java:69)
                at rx.internal.schedulers.ScheduledAction.run(ScheduledAction.java:55)
                at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
                at java.util.concurrent.FutureTask.run(FutureTask.java:266)
                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
                at java.lang.Thread.run(Thread.java:748)
        */
        System.out.println("fallback: "+throwable.getMessage());//对message相关信息做打印，打印异常信息，即将throw new RuntimeException("message")中的message进行了打印："fallback: 服务异常了。"

        return new ResultObject(Constant.ONE, "服务降级了");//响应码为0表示响应正常，响应码为1表示响应是不正常的
    }


    /*
    * 该为ribbon+restTemplate进行实现远程服务调用
    * 通过ribbon+restTemplate进行测试hystrix修改默认配置超时时间为5秒是否生效
    * 即以下配置
    ribbon.ReadTimeout=6000
    ribbon.ConnectTimeout=3000

    hystrix.command.default.execution.timeout.enabled=true
    hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000
    先通过ribbon+restTemplate进行测试，重新编译portal服务
    portal服务重新启动后测试接口：http://localhost:8080/cloud/goodsRibbon
    返回响应数据：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
    即说明没有触发Hystrix的服务降级。即上述配置hystrix超时时间为5秒生效了。
    ribbon.ReadTimeout为6秒，hystrix设置的超时时间为5秒，谁的值小取谁的值生效，所以此时hystrix超时时间为5秒生效了。
    远程服务超时时间为2秒，hystrix超时时间为5秒，大于远程服务超时时间，所以不会触发hystrix的服务降级。

    这当中有个坑，即取ribbo.ReadTimeout和execution.isolation.thread.timeoutInMilliseconds两个配置值的最小值为准。
    所以就导致了这两个配置项都需要进行配置一下，即ribbon.ReadTimeout默认也是1秒，hystrix默认超时时间也是1秒，所以都需要改。
    不管是使用ribbon+restTemplate调用远程服务还是通过feign声明式调用远程服务，它其中底层都是使用ribbon来去进行做负载均衡的。将ribbon.ReadTimeout和execution.isolation.thread.timeoutInMilliseconds两个配置项的取值都改动一下。

    上述即为修改hystrix的默认超时时间。通过配置文件进行修改的取值。
    另外还有一个办法即为通过代码去进行修改默认超时时间。
    即通过注解@HystrixCommand中的配置项去进行修改默认超时时间（现将配置文件applicatio.properties中关于hystrix配置默认超时时间的配置项进行注释）
    即在@HystrixCommand中配置默认超时时间就相当于 application.properties当中的配置项: [hystrix.command.default.execution.timeout.enabled=true、hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000]
    当然ribbon的读取超时还是要在application.properties当中进行配置的，也就是说ribbon.ReadTimeout=6000在application.properties配置文件当中不能进行注释。
    因为hystrix的默认超时时间是根据execution.isolation.thread.timeoutInMilliseconds和ribbon.ReadTimeout两个配置项的取值比较大小得来的，谁小取谁，所以在配置文件application.properties当中还是需要表明ribbon.ReadTimeout读取超时时间时长

    * */
    @HystrixCommand(fallbackMethod = "fallback")
    @RequestMapping("/cloud/goodsRibbon")
    public ResultObject goodsRibbon() {
        //调用远程的一个controller，RESTful的调用
        ResponseEntity<ResultObject> resposneEntity= restTemplate.getForEntity(GOODS_SERVICE_URL_02,ResultObject.class);
        return resposneEntity.getBody();
    }


    /*
    * 问题：那如果 调用的远程服务宕机了，它还怎么去走下面的方法呢？
    * 即如果远程服务中goods9100和goods9200服务都宕机了，那么是否会触发Hystrix的服务降级，以及如果不会触发Hystrix的服务降级那么会返回前端远程服务宕机的报错信息吗？
    * 即测试一下，将goods9100和goods9200的服务进行关闭。单独留下portal服务。
    * 即goods9100:Process finished with exit code -1   goods9200:Process finished with exit code -1 仅有portal服务是开启的。刷新下面的测试地址会发现走的是服务降级
     * 这个时候访问地址：http://localhost:8080/cloud/goodsHystrix 响应数据如下:{"statusCode":1,"statusMessage":"服务降级了","data":null}
    * 也是会触发Hystrix的服务降级。
    * 即当远程服务不可用的时候、抛出异常的时候、响应超时的时候，都将会触发Hystrix的服务降级。
    * 消费者服务抛出异常、响应超时的时候也会触发Hystrix的服务降级。
    *
    * 那么这就是Hystrix提供的服务降级功能。即服务的熔断降级这样一个功能。
    * 远程服务不可用、远程服务超时、远程服务异常等情况都可以进行触发Hystrix的服务降级。触发服务降级之后，需要有一个备用的方法即@HystrixCommand注解中fallbackMethod配置项的内容，fallback()方法
    * 当服务降级被触发该fallback()备用的方法就会被调用返回给前端默认的数据。
    *
    *
    * 服务宕机就相当于请求超时，404？
    * 宕机即访问服务请求超时，没有响应。因为是controller调用controller，远程服务停止运行了，然而浏览器端进行直接访问远程服务的接口地址，也会出现打不开，是会进行请求一会儿，即请求超时之后才提示打不开该页面，即访问不到该页面
    * 访问不到那么就会触发HystrixCommand的服务降级，请求超时触发服务降级，然后调用备用方法。
    * 404是代表路径找不到。
    * 以上就是测试的 Hystrix的服务降级
    * */


    /**
     * 查询所有商品
     * @HystrixCommand注解当中有多个配置项可以进行配置，配置项直接通过逗号进行分隔开来
     * @HystrixCommand注解当中threadPoolProperties配置项是一个数组，当中可以使用{}花括号配置多个属性。
     threadPoolKey  给线程池一个key,唯一的key,相当于是一个名字具体含义可以该配置项当中的说明点进去即可
                    the thread-pool key is used to represent a HystrixThreadPool for monitoring , metrics  publishing , caching  and other  such uses.
                    线程池的这个key用于去表达代表一个Hystrix线程池为监控去使用的，发送、caching以及别的一些用途；可以将threadPoolKey认为是一个标记，名字的一个标记
                    represent：代表 | monitoring：监控 | HystrixThreadPool hystrix线程池 | publishing 发送 | other such uses 别的使用
                    可以认为是一个标记，一个名字

    threadPoolProperties 即线程池的一些属性，是一个数组，
                    //Specifies thread pool properties 指定线程池的一些属性；
                    HystrixProperty[] threadPoolProperties() default{};
                    该属性是一个数组，该数组取值本身也是一个注解，用注解去表明的，即public @interface HystrixProperty{ String name(); //property name @return name  String value();//property value @return value}
                    所以threadPoolProperties该配置项当中也是通过注解去写@HystrixProperty，有name、value和值
                    @HystrixProperty  name=coreSize  value=2 核心线程数大小为2，为了测试，因为写多的话手没有那么快测试不了效果在实际项目开发的时候可以将该取值写大一点，这里是为了测试效果所以写两个
                    @HystrixProperty name=maxQueueSize value=1 最大队列大小，大小为1，含义是 同时发送了三个请求，那么它是可以处理的，发送第四个的时候就无法处理了，coreSize即核心线程数本来有两个两个线程可以进行处理，然后另外有一个队列，这个队列即maxQueueSize，这个队列当中又可以放一份，那么这样的话三个请求分别被放在了coreSize两个线程，以及maxQueueSize一个队列上
                    即同时有三个请求过来，这三个请求会分别落在coreSize 线程1 线层2 以及一个 maxQueueSize队列上。此时不会进行限流；
                    但是当发送第四个请求过来的时候，这个时候就会触发hystrix的限流，因为这个时候前面三个请求都分别落在了thread1、thread2、queue队列上，前面三个都还没有处理完成，即没有腾出thread1、thread2、queue中的某一个出来。
                    queue队列当中已经放满了，只能放置一个请求。
                    那么此时再来第四个请求的时候，就放不下了。线程coreSize 两个线程又没有空闲下来的。queue队列当中最多又只能放一个，所以此时就会触发hystrix的限流。
                    限制第四个请求进来的时候提示限流了。基于这个原理。
                    所以等会儿刷新，刷新到第四次的时候就会发现请求不了了。前面三个请求是可以请求的
                    此时可以进行测试。重新启动portal服务后测试访问地址：http://localhost:8080/cloud/goodsLimit  该请求地址连续刷新四次即可。快速刷新四次就会触发限流
                    http://localhost:8080/cloud/goodsLimit
                    此时刷新第一遍的响应结果如下：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
                    这个时候两秒是可以拿到结果的，然后反复快速刷新几次就会发生限流。即按住f5 连续按4下不要停下，那么第四下后停下就会看到浏览器响应如下：{"statusCode":1,"statusMessage":"服务降级了","data":null}
                    第四遍限流了。内部没有线程数可用了，队列也被放满了。直接会返回限流，即服务降级的内容。
                    第四次刷新的时候没有线程数可以用了。因为每一次请求线程都要走两秒才会响应给前端，即远程服务当中有做超时两秒的操作，所以会要等待两秒，而我们快速刷新4次的时候是并没有等待这个每个请求的两秒时间的，所以就好像是一次性可以容纳有3个请求进去，如果不等待两秒的话，里面最多可以执行三个请求，即coreSize和maxQueueSize。
                    也就是说类似于 并发数是3，那么超过了这个数值的话，就会返回限流，走服务降级。前面三个请求都没有等待两秒的响应就快速刷新过去了被放到了线程1线程2和队列中。
                    因为每一个线程要走两秒才会走完，而maxQueueSize只是一个可用空间，该队列当中只可以放一个请求。即总共可以有三次请求，在刷新第四次的时候就不行了。
                    即就是在两秒内刷新四次那么这个时候就会触发hystrix的限流从而服务降级

                    threadPoolKey是一个标识，hystrix通过这个标识来进行累计计数的，看线程是否超过了，如果线程超过了这个大小，超过了就会直接进行降级即在第四次的时候降级了。
                    通过线程数来进行限流。

     * 请求方法为：/cloud/goodsLimit
     * @return
     */
    @HystrixCommand(
            fallbackMethod = "fallback",
//            commandProperties = {
//                    @HystrixProperty(name = "execution.timeout.enabled", value = "true"),
//                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")
//            },
            //@HystrixCommand注解配置限流的配置项参数threadPoolKey、threadPoolProperties；在@HystrixCommand注解当中每一个配置项都通过英文逗号进行隔开
            //@HystrixCommand当中的配置项threadPoolProperties该参数取值是一个数组所以用花括号{}，数组当中的取值类型为@HystrixProperty也是一个注解的形式
            //threadPoolKey相当于给线程池一个key，即相当于给它唯一的一个key，相当于一个名字，具体含义可以点击进去看一下它的说明
            //threadPoolProperties 线程池的一些属性，是一个数组
            threadPoolKey = "goods",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "2"),
                    @HystrixProperty(name = "maxQueueSize", value = "1")
            })
    @RequestMapping("/cloud/goodsLimit")
    public ResultObject goodsLimit(){
        //调用远程的一个controller，restful的调用
        ResponseEntity<ResultObject> responseEntity = restTemplate.getForEntity(GOODS_SERVICE_URL_02, ResultObject.class);

        return responseEntity.getBody();

    }


    /**
     * 查询所有商品
     * 当前不再需要@HystrixCommand注解了，因为hystrix.command.default.exuecution.isolation.thread.timeoutInMilliseconds已经写在了配置文件当中进行配置了；
     * 而fallbackMethod现在是测试的是通过feign进行整合hystrix，即在feign声明式接口当中配置好了fallback的相关类，GoodsRemoteClientFallBack类当中实现GoodsRemoteClient接口当中的goods()方法
     * 通过feign的方式进行触发hystrix的服务降级，@FeignClient注解配置了fallback为GoodsRemoteClientFallBack类
     *
     * 最主要是看goodsRemoteClient feign调用接口，该接口会去调用远程方法的goods()，如果远程方法出现有问题，那么goodsRemoteClient会不会走服务降级，如果走了服务降级即将会走GoodsRemoteClientFallBack类当中的goods()方法
     * 即将打印日志"feign 服务调用降级"
     * 将消费者portal重新编译并启动 测试访问地址：http://localhost:8080/cloud/goodsFeignHystrix 响应如下：{"statusCode":0,"statusMessage":"查询成功","data":[{"id":1,"name":"商品1","price":67.0,"store":12},{"id":2,"name":"商品2","price":168.0,"store":1},{"id":3,"name":"商品3","price":25.0,"store":50}]}
     * 响应结果数据正确。但是和需要测试出来的结果是有问题的
     *
     * hystrix默认超时时间是1秒钟，此时要测试的是远程服务调用方法需要两秒钟，feign调用方法超时导致触发服务降级，即调用GoodsRemoteClientFallBack类当中的goods()服务降级方法
     * 即将portal服务当中的application.properties文件中的ribbon.ReadTimeout 以及 hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds配置项进行注释，注释完成之后重新编译portal服务并重新启动
     * 注释之后hystrix当前默认的超时时间为1秒钟，而调用远程方法需要两秒钟，此时测试的是看是否因为feign调用远程服务超时从而触发hystrix服务降级从而调用配置在注解@FeignClient注解当中的配置项fallback GoodsRemoteClientFallBack类当中的goods()服务降级方法
     * 再次访问测试地址：http://localhost:8080/cloud/goodsFeignHystrix 此时浏览器页面响应如下：
     * Whitelabel Error Page
     * This application has no explicit mapping for /error, so you are seeing this as a fallback.
     *
     * Thu May 27 08:53:51 CST 2021
     * There was an unexpected error (type=Internal Server Error, status=500).
     * Read timed out executing GET http://34-SPRINGCLOUD-SERVICE-GOODS/service/goods
     * feign.RetryableException: Read timed out executing GET http://34-SPRINGCLOUD-SERVICE-GOODS/service/goods
     * 	at feign.FeignException.errorExecuting(FeignException.java:132)
     * 	at feign.SynchronousMethodHandler.executeAndDecode(SynchronousMethodHandler.java:113)
     * 	at feign.SynchronousMethodHandler.invoke(SynchronousMethodHandler.java:78)
     * 	at feign.ReflectiveFeign$FeignInvocationHandler.invoke(ReflectiveFeign.java:103)
     * 	at com.sun.proxy.$Proxy135.goods(Unknown Source)
     * 	at com.bjpowernode.springcloud.controller.GoodsController.goodsFeignHystrix(GoodsController.java:641)
     * 	at com.bjpowernode.springcloud.controller.GoodsController$$FastClassBySpringCGLIB$$728203a2.invoke(<generated>)
     * 	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
     * 	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:750)
     * 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
     * 	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:93)
     * 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
     * 	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:689)
     * 	at com.bjpowernode.springcloud.controller.GoodsController$$EnhancerBySpringCGLIB$$b89ae6eb.goodsFeignHystrix(<generated>)
     * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     * 	at java.lang.reflect.Method.invoke(Method.java:498)
     * 	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190)
     * 	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138)
     * 	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:105)
     * 	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:893)
     * 	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:798)
     * 	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
     * 	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1040)
     * 	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:943)
     * 	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)
     * 	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:898)
     * 	at javax.servlet.http.HttpServlet.service(HttpServlet.java:634)
     * 	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)
     * 	at javax.servlet.http.HttpServlet.service(HttpServlet.java:741)
     * 	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231)
     * 	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
     * 	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)
     * 	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
     * 	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
     * 	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
     * 	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
     * 	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
     * 	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
     * 	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)
     * 	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
     * 	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
     * 	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
     * 	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:94)
     * 	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
     * 	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
     * 	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
     * 	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)
     * 	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
     * 	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
     * 	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
     * 	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)
     * 	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
     * 	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:526)
     * 	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:139)
     * 	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)
     * 	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
     * 	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343)
     * 	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:408)
     * 	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
     * 	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:860)
     * 	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1589)
     * 	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
     * 	at java.lang.Thread.run(Thread.java:748)
     * Caused by: java.net.SocketTimeoutException: Read timed out
     * 	at java.net.SocketInputStream.socketRead0(Native Method)
     * 	at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
     * 	at java.net.SocketInputStream.read(SocketInputStream.java:171)
     * 	at java.net.SocketInputStream.read(SocketInputStream.java:141)
     * 	at java.io.BufferedInputStream.fill(BufferedInputStream.java:246)
     * 	at java.io.BufferedInputStream.read1(BufferedInputStream.java:286)
     * 	at java.io.BufferedInputStream.read(BufferedInputStream.java:345)
     * 	at sun.net.www.http.HttpClient.parseHTTPHeader(HttpClient.java:735)
     * 	at sun.net.www.http.HttpClient.parseHTTP(HttpClient.java:678)
     * 	at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1587)
     * 	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1492)
     * 	at java.net.HttpURLConnection.getResponseCode(HttpURLConnection.java:480)
     * 	at feign.Client$Default.convertResponse(Client.java:143)
     * 	at feign.Client$Default.execute(Client.java:68)
     * 	at org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer.execute(FeignLoadBalancer.java:93)
     * 	at org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer.execute(FeignLoadBalancer.java:56)
     * 	at com.netflix.client.AbstractLoadBalancerAwareClient$1.call(AbstractLoadBalancerAwareClient.java:104)
     * 	at com.netflix.loadbalancer.reactive.LoadBalancerCommand$3$1.call(LoadBalancerCommand.java:303)
     * 	at com.netflix.loadbalancer.reactive.LoadBalancerCommand$3$1.call(LoadBalancerCommand.java:287)
     * 	at rx.internal.util.ScalarSynchronousObservable$3.call(ScalarSynchronousObservable.java:231)
     * 	at rx.internal.util.ScalarSynchronousObservable$3.call(ScalarSynchronousObservable.java:228)
     * 	at rx.Observable.unsafeSubscribe(Observable.java:10327)
     * 	at rx.internal.operators.OnSubscribeConcatMap$ConcatMapSubscriber.drain(OnSubscribeConcatMap.java:286)
     * 	at rx.internal.operators.OnSubscribeConcatMap$ConcatMapSubscriber.onNext(OnSubscribeConcatMap.java:144)
     * 	at com.netflix.loadbalancer.reactive.LoadBalancerCommand$1.call(LoadBalancerCommand.java:185)
     * 	at com.netflix.loadbalancer.reactive.LoadBalancerCommand$1.call(LoadBalancerCommand.java:180)
     * 	at rx.Observable.unsafeSubscribe(Observable.java:10327)
     * 	at rx.internal.operators.OnSubscribeConcatMap.call(OnSubscribeConcatMap.java:94)
     * 	at rx.internal.operators.OnSubscribeConcatMap.call(OnSubscribeConcatMap.java:42)
     * 	at rx.Observable.unsafeSubscribe(Observable.java:10327)
     * 	at rx.internal.operators.OperatorRetryWithPredicate$SourceSubscriber$1.call(OperatorRetryWithPredicate.java:127)
     * 	at rx.internal.schedulers.TrampolineScheduler$InnerCurrentThreadScheduler.enqueue(TrampolineScheduler.java:73)
     * 	at rx.internal.schedulers.TrampolineScheduler$InnerCurrentThreadScheduler.schedule(TrampolineScheduler.java:52)
     * 	at rx.internal.operators.OperatorRetryWithPredicate$SourceSubscriber.onNext(OperatorRetryWithPredicate.java:79)
     * 	at rx.internal.operators.OperatorRetryWithPredicate$SourceSubscriber.onNext(OperatorRetryWithPredicate.java:45)
     * 	at rx.internal.util.ScalarSynchronousObservable$WeakSingleProducer.request(ScalarSynchronousObservable.java:276)
     * 	at rx.Subscriber.setProducer(Subscriber.java:209)
     * 	at rx.internal.util.ScalarSynchronousObservable$JustOnSubscribe.call(ScalarSynchronousObservable.java:138)
     * 	at rx.internal.util.ScalarSynchronousObservable$JustOnSubscribe.call(ScalarSynchronousObservable.java:129)
     * 	at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
     * 	at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
     * 	at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
     * 	at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
     * 	at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
     * 	at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
     * 	at rx.Observable.subscribe(Observable.java:10423)
     * 	at rx.Observable.subscribe(Observable.java:10390)
     * 	at rx.observables.BlockingObservable.blockForSingle(BlockingObservable.java:443)
     * 	at rx.observables.BlockingObservable.single(BlockingObservable.java:340)
     * 	at com.netflix.client.AbstractLoadBalancerAwareClient.executeWithLoadBalancer(AbstractLoadBalancerAwareClient.java:112)
     * 	at org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient.execute(LoadBalancerFeignClient.java:83)
     * 	at feign.SynchronousMethodHandler.executeAndDecode(SynchronousMethodHandler.java:108)
     * 	... 66 more
     *
     * 	发现抛出了一个异常，该异常和访问超时有关：Caused by: java.net.SocketTimeoutException: Read timed out
     * 	此时仍然没有使用到feign @FeignClient注解配置项fallback，即调用到GoodsRemoteClientFallBack类当中的fallback方法进行服务降级
     * 	此时没有触发@FeignClient注解配置的fallback 服务降级
     *
     * 	原因如下：feign默认是支持hystrix的，按时在Spring Cloud Dalston 版本之后就默认关闭了，因为不一定业务需求要用得到。所以现在要使用首先得打开它，在属性文件application.properties加上如下配置
     * 	feign.hystrix.enabled=true
     * 	所以此时去portal服务的application.properties中去进行配置该配置项
     * @return
     */
    @RequestMapping("/cloud/goodsFeignHystrix")
    public ResultObject goodsFeignHystrix(){
        return goodsRemoteClient.goods();
    }



}
