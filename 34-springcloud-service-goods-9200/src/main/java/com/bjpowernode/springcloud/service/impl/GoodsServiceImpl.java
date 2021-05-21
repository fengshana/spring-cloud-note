package com.bjpowernode.springcloud.service.impl;

import com.bjpowernode.springcloud.mapper.GoodsMapper;
import com.bjpowernode.springcloud.model.Goods;
import com.bjpowernode.springcloud.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    public List<Goods> getAllGoods(){
        return goodsMapper.selectAllGoods();
    }

    public Goods getGoodsById(Integer goodsId){
        return goodsMapper.selectByPrimaryKey(goodsId);
    }
}