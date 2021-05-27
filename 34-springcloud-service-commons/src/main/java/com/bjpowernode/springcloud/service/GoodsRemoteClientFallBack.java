package com.bjpowernode.springcloud.service;

import com.bjpowernode.springcloud.constants.Constant;
import com.bjpowernode.springcloud.model.ResultObject;
import org.springframework.stereotype.Component;

/**
 * 服务降级类
 *
 * 该类如何实现
 * hystrix此时走服务降级即走到该类当中来
 *
 * GoodsRemoteClientFallBack需要实现接口GoodsRemoteClient接口，即实现implements GoodsRemoteClient接口，然后重写覆盖goods方法。
 * GoodsRemoteClient 接口当中即goods()方法需要进行实现
 * 即其实非常明显@FeignClient GoodsRemoteClient在调用远程方法goods()时，如果远程服务异常、超时、服务不可用了那么这个时候，
 * 则@FeignClient有一个备胎，即@FeignClient注解当中配置了fallback即服务降级类 GoodsRemoteClientFallBack类，到时候就会走该类当中的goods方法，
 * GoodsRemoteClientFallBack因为实现了GoodsRemoteClient接口，所以goods方法的方法类型、方法返回参数、方法参数都是和GoodsRemoteClient接口当中的goods方法一样
 * 也就是说，远程服务goods9100和9200的GoodsController当中的也相当于是实现了GoodsRemoteClient接口当中的goods()方法，存在有goods()方法的实现
 * 而此时在GoodsRemoteClientFallBack也实现了GoodsRemoteClient接口当中的方法，只不过将该类当中重写的goods()方法作为服务降级方法，即调用远程服务失败后服务降级即调用该类当中的方法goods()
 *
 * GoodsRemoteClientFallBack类当中重写goods()方法在服务降级的时候被调用和在portal消费者中的controller的@HystrixCommand注解中配置的fallbackMethod方法差不太多。
 * GoodsRemoteClientFallBack这种方式的话就相当于从业务代码当中拿出来了。不掺和接口当中去
 * 感觉是做了一层封装。
 * 在portal的controller中的@HystrixCommand注解中fallbackMethod配置项中虽然配置了fallback()方法的名称，但是fallback还需要自己去规定好需要和goodsHystrix()方法的方法访问类型、返回参数类型、方法参数一致
 * 但是GoodsRemoteClientFallBack进行实现GoodsRemoteClient接口中的方法这样子的方式就避免了自己没有对应好portal服务中的controller中的goodsHystrix()方法的方法类型、返回参数、方法参数这些不统一的情况
 * 比之前的那种直接写在portal服务的controller中更好一些。
 *
 * 如果GoodsRemoteClient接口当中的goods()方法调用远程方法失败了，也就是远程服务异常了超时了到时候还有一个备胎，备胎就是GoodsRemoteClientFallBack当中的goods()方法
 * 到时候就调用备胎当中的goods()方法即可。
 * 即如果GoodsRemoteClient当中的goods 调用远程方法超时那么就调用GoodsRemoteClientFallBack当中的goods()方法
 * 即GoodsRemoteClientFallBack当中的goods即备用方法
 * 在该类GoodsRemoteClientFallBack 类上面标注一个组件注解，让spring容器当中去进行扫描这个类，不然这个类GoodsRemoteClientFallBack没有被识别出来也不行
 * 也会有问题
 * 然后在GoodsRemoteClientFallBack类当中的goods()方法中写方法实现，那么此时的方法实现就和portal服务中的controller接口中@HystrixCommand注解中配置的fallbackMethod="fallback"
 * fallback()方法中的实现一致即可。
 *
 */
@Component
public class GoodsRemoteClientFallBack implements GoodsRemoteClient{


    /**
     * 备用方法，即也就是服务降级方法
     * 到时候GoodsRemoteClient当中的goods()方法调用远程服务的goods()实现方法失败那么则会调用GoodsRemoteClientFallBack中的goods()方法进行返回给前端
     * 此时进行重启一下portal服务看一下有没有效果，测试访问地址：http://localhost:8080/cloud/goodsHystrix
     *
     * 由于修改了GoodsRemoteClient类还新增了GoodsRemoteClientFallBack类，也就是对commons服务进行了修改，
     * 即goods9100、goods9200、portal服务都依赖于commons项目，所以热部署插件此时使用可能会不正常，所以进行重启goods9100、goods9200、portal服务即可
     * 重启完成之后进行测试消费者portal当中的controller，
     * @return
     */
    @Override
    public ResultObject goods() {
        return new ResultObject(Constant.ONE, "feign 服务调用降级");
    }
}
