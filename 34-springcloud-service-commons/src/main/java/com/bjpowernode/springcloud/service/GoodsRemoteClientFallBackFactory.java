package com.bjpowernode.springcloud.service;

import com.bjpowernode.springcloud.constants.Constant;
import com.bjpowernode.springcloud.model.ResultObject;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;


/**
 * 实现接口 FallBackFactory<T> 并重写该接口fallback工厂中的create()方法，覆盖create方法
 * 在create方法当中就将Throwable 父级异常作为方法参数传递到create方法当中来了。
 * 也就是说可以从这当中拿到异常信息
 */
@Component
public class GoodsRemoteClientFallBackFactory implements FallbackFactory<GoodsRemoteClient> {


    /**
     * 由于在实现FallbackFactory<T>接口重写该接口当中的create方法时，需要进行返回GoodsRemoteClient，即声明式接口类
     * 即返回 声明式接口类的一个实例对象。
     * 返回声明类的接口对象。所以在该工厂当中去new一个GoodsRemoteClient，相当于一个匿名内部类去进行实现GoodsRemoteClient接口当中的goods()方法即可
     * 去覆盖其goods()方法即可
     * 和GoodsRemoteClientFallBack的做法差不多。
     * 在匿名内部类当中就可以去获取到调用远程服务异常的信息，即throwable.getMessage()
     * throwable即被作为参数传入到create方法当中来。
     * 该GoodsRemoteClientFallBackFactory工厂中的create()方法即获取远程服务调用异常信息 并返回服务降级的默认结果 的方法
     * 该方法在调用远程服务降级的时候进行时候，即调用远程服务异常情况下使用。
     *
     * feign和hystrix 获取远程服务异常信息：
     * 首先注意 feign.hystrix.enabled=true该配置项注意需要开启，否则@FeignClient注解上关于hystrix的配置项不起作用
     * 通过在feign接口上的@FeignClient注解中的配置项fallbackFactory配置项 取值为实现了FallBackFactory<T> fallback工厂接口的实现类
     * 该fallback工厂实现类当中去进行重写create方法，方法参数为Throwable，该参数可以拿取到调用远程服务异常的相关信息
     * 拿取到异常信息之后可以根据异常信息做出处理。且该create()方法和注解@FeignClient中配置项fallback, 实现了GoodsRemoteClient feign接口的GoodsRemoteClientFallBack类中重写的goods()方法一样
     * create方法需要返回GoodsRemoteClient接口的一个实例对象，所以此时将在create方法当中需要生成GoodsRemoteClient的实例对象，也就是匿名内部类对象。
     * 通过实现FallBackFactory接口重写create方法即可拿取得到调用远程服务异常的信息，
     * 也就是说实现FallBackFactory接口当中重写create方法需要返回GoodsRemoteClient接口实例对象，即相当于此处也是一个服务降级的一个返回
     * 因为在返回GoodsRemoteClient实例对象时，通过匿名内部类的方式重写goods方法，在goods方法当中返回给前端return new ResultObject对象。默认的返回值，这也是一个服务降级的返回
     *
     *
     * feign和hystrix 实现服务降级：
     * 首先application.properties中配置feign.hystrix.enabled=true
     * 再在@FeignClient feign的接口上的注解中配置 配置项fallback,该fallback的取值为 实现了GoodsRemoteClient接口的实现类，在该接口类当中重写了goods方法即该方法为服务降级方法返回默认值
     *
     * @param throwable
     * @return
     */
    @Override
    public GoodsRemoteClient create(Throwable throwable) {
        return new GoodsRemoteClient() {
            @Override
            public ResultObject goods() {
                String message = throwable.getMessage();//拿到异常信息之后将异常信息打印出来
                System.out.println("feign 远程调用异常："+message);
                return new ResultObject(Constant.ONE, "服务异常啦");//进行返回服务降级的默认结果
            }
        };
    }
    /**
     上述完成之后，由于该类需要被spring容器进行识别，所以要在该实现类上添加@Component注解
     不能识别的话则无法使用

     此时由于测试在远程服务异常时feign声明式调用时获取得到了远程服务异常的信息，则首先还需要看远程服务有没有异常
     远程goods9100和goods9200此时仅存在一个休眠两秒，并没有打开异常。此时去goods9100、9200打开异常
     打开异常之后，重新启动portal、goods9100、goods9200服务
     重启之后访问测试地址：http://localhost:8080/cloud/goodsFeignHystrix 响应如下：{"statusCode":1,"statusMessage":"服务异常啦","data":null}
     基于feign声明式接口调用的时候，feign要获取远程服务异常信息该如何拿取，如何服务降级如何获取异常信息
     可以根据响应信息可以看到statusMessage即为GoodsRemoteClientFallBackFactory当中create方法中返回的GoodsRemoteClient匿名内部类实例对象，因为匿名内部类实例对象当中重写了GoodsRemoteClient接口的goods()方法，该方法即服务降级方法。

     @Override
     public ResultObject goods() {
     String message = throwable.getMessage();//拿到异常信息之后将异常信息打印出来
     System.out.println("feign 远程调用异常："+message);
     return new ResultObject(Constant.ONE, "服务异常啦");//进行返回服务降级的默认结果
     }
     该段即为降级，响应当中的statusMessage即为降级方法当中的响应内容。
     且在portal服务的控制台当中看到输出日志：feign 远程调用异常：null
     打印的是 throwable.getMessage()，该message没有信息；远程服务拿到了，但是远程服务的异常信息没有拿到
     该message应该会把远程服务当中 throw new RuntimeException(message)当中的message给打印出来
     而这里并没有获取到。 调用正常仅远程信息调用异常的信息没有拿到

     */
}
