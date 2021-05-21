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
     * @param model
     * @return
     */
    @RequestMapping("/service/goods")
//    public String goods(Model model){
        public ResultObject goods(Model model){
        //为了在调用服务提供者的服务的时候区分9100的goods服务和9200的goods服务，在这里打印信息以作区分。用以区分负载均衡到底是调用的哪一台服务提供者的服务，是9100还是9200
        System.out.println("9100 被执行...............................");

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
