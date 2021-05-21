package com.bjpowernode.springcloud.controller;

import com.bjpowernode.springcloud.model.ResultObject;
import com.bjpowernode.springcloud.service.GoodsRemoteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

}
