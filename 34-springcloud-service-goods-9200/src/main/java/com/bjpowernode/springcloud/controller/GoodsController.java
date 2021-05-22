package com.bjpowernode.springcloud.controller;

import com.bjpowernode.springcloud.constants.Constant;
import com.bjpowernode.springcloud.model.Goods;
import com.bjpowernode.springcloud.model.ResultObject;
import com.bjpowernode.springcloud.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@Controller
@RestController
public class GoodsController {
    //基于RESTful API 风格，所以@Controller需要替换成 @RestController
    //返回JSON，用JSON的方式进行接口之间的数据交互，是Controller 调用 Controller

    @Autowired
    GoodsService goodsService;
    //GoodsController 调用 GoodsService
    //接着GoodsService 查询数据 getAllGoods()
    //数据查询到以后，并不需要将goodsList放到model当中，直接将该数据返回即可
    //使用ResutlObject 作为返回值

    /**
     * 查询所有商品
//     * @param model
     * @return
     */
    @RequestMapping("/service/goods")
//    public String goods(Model model){
//        public ResultObject goods(Model model){
    public ResultObject goods(){ //当前由于Model model 没有使用到该参数，所以进行删除，以及GoodsController是GoodsRemoteClient接口的实现，为了保持统一，所以进行删除
        //为了在调用服务提供者的服务的时候区分9100的goods服务和9200的goods服务，在这里打印信息以作区分。用以区分负载均衡到底是调用的哪一台服务提供者的服务，是9100还是9200
        System.out.println("9200 被执行...............................");


//      进行测试远程服务中方法响应数据响应超时，从而在消费者端带有注解@HystrixCommand的goodsHystrix()方法会进行服务降级处理，即返回fallback()方法默认响应数据
//        try {
//            Thread.sleep(2000);//沉睡休眠2s
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//      进行测试远程服务中方法响应时抛出异常，是否会触发Hystrix的服务降级。测试远程服务方法异常时goods9100和goods9200都需要写抛出异常代码。原因在于feign底层是通过ribbon负载均衡调用服务提供者服务。
//        String str = null;
//        if(str == null){
//            throw new RuntimeException("服务异常了");
/*            那么这个时候也会走服务降级。进行测试是否响应数据为服务降级之后的默认数据。
              同样还是点击Build当中的Build Module 34-sringcloud-service-portal即可，通过spring boot的自动热部署工具进行自动重启服务即可。
              portal服务启动好之后，测试服务地址：httpL://localhost:8080/cloud/goodsHystrix，返回响应数据：{"statusCode":1,"statusMessage":"服务降级了","data":null}
              从响应数据中可以发现，此操作即消费者服务异常确实是会引起Hystrix进行服务降级，即走入到@HystrixCommand命令中配置的fallbackMethod中的fallback方法中。
*/
//        }

        /*
        开启沉睡两秒钟，为了测试portal服务中application.properties中的配置项是否生效
        * hystrix.command.default.execution.timeout.enabled=true
        * hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000 hystrix修改默认超时时间为5秒
        * 由于远程服务仅沉睡休眠两秒钟，而hystrix修改了其默认超时时间为5秒钟，所以不会因为远程服务的响应超时而导致触发hystrix的服务降级，从而返回fallback数据给前端
        * 而是会返回正常的响应数据给前端;也就是不会触发服务降级
        * 测试即启动goods9100和goods9200，以及重新编译portal服务
        * */
        try {
            Thread.sleep(2000);//沉睡休眠2s
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        List<Goods> goodsList = goodsService.getAllGoods();
//        model.addAttribute("goodsList", goodsList);
//        return "goods";

        //此处返回响应码是一个常量类constants ，将它放到commons项目当中的constants包下的Constant类
        //此处定义 Constant.ZERO 代表成功的响应码
        return new ResultObject(Constant.ZERO, "查询成功", goodsList);//构造方法

        //目前看到的依然是SpringBoot项目，还没有看到Spring Cloud，Spring Cloud 还没有真正使用得到。
        //SpringCloud即在这个基础之上，再加一些东西，为了解决分布式微服务当中的一些问题，里面的开发依旧是使用的SpringBoot进行开发的。
        //下面再到goods服务当中的application.properties当中配置一下数据库的连接
        //目前来说依旧是一个springBoot项目，可以单独进行跑起来，运行起来
    }

}
