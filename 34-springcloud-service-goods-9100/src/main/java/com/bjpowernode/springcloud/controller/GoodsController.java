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
        System.out.println("9100 被执行...............................");


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
        * 测试访问地址：http://localhost:8080/cloud/goodsHystrix 响应数据如下：{"statusCode":1,"statusMessage":"服务降级了","data":null}
        * 发现服务仍然是降级了。
        * */
        try {
            Thread.sleep(2000);//沉睡休眠2s
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        /*
        测试远程服务抛出异常，从而触发hystrix的服务降级，看是否fallback方法的Throwable能拿到远程服务的异常信息
        远程服务goods集群部署有9100和9200所以9200也需要抛出异常测试
        然后将9100、9200服务、portal服务rebuild 重新自动部署，即选中 34-springcloud-service-goods-9100 idea左侧的项目名称 ---->IDEA Build ---->Build Module 34-springcloud-service-goods-9100
        34-springcloud-service-goods-9200 idea左侧的项目名称 ---->IDEA Build ---->Build Module 34-springcloud-service-goods-9200
        34-springcloud-service-portal idea左侧的项目名称 ---->IDEA Build ---->Build Module 34-springcloud-service-portal
        如果需要build哪个项目选中它的项目名称再进行build即可,三台服务都会进行重启
        idea左侧的项目名称 ---->IDEA Build ---->Build Module "需要进行build的项目名称"
        重启完成之后，再次访问测试接口地址：http://localhost:8080/cloud/goodsHystrix 响应如下：{"statusCode":1,"statusMessage":"服务降级了","data":null}
        即触发了服务降级
        portal服务中查看控制台的打印信息如下，消费者打印的信息是这样的 即fallback可以拿取到远程的异常信息，只会提示 500报错；真正的 runtimeException错误信息还是需要到 goods服务的控制台去看
        但是尽管如此 throwable 是可以拿取到 消费方和远程服务方的异常信息的
        throwable.printStackTrace(); ----->
        feign.FeignException$InternalServerError: status 500 reading GoodsRemoteClient#goods()
            at feign.FeignException.errorStatus(FeignException.java:114)
            at feign.FeignException.errorStatus(FeignException.java:86)
            at feign.codec.ErrorDecoder$Default.decode(ErrorDecoder.java:93)
            at feign.SynchronousMethodHandler.executeAndDecode(SynchronousMethodHandler.java:149)
            at feign.SynchronousMethodHandler.invoke(SynchronousMethodHandler.java:78)
            at feign.ReflectiveFeign$FeignInvocationHandler.invoke(ReflectiveFeign.java:103)
            at com.sun.proxy.$Proxy131.goods(Unknown Source)
            at com.bjpowernode.springcloud.controller.GoodsController.goodsHystrix(GoodsController.java:348)
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

        System.out.println(throwable.getMessage()); ---->  fallback: status 500 reading GoodsRemoteClient#goods()

        以上测试fallback中的throwable拿取远程服务方的异常信息就测试完成了
        */
//        String str = null;
//        if(str == null){
//            throw new RuntimeException("远程Goods9100服务异常了");
//        }


        /**
         * 进行测试feign整合hystrix获取远程服务异常信息，所以需要设置远程服务goods9100、9200存在有异常
         */
        String str = null;
        if(str == null){
            throw new RuntimeException("远程Goods9100服务异常了");
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
