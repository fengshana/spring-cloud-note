package com.bjpowernode.springcloud.service;

import com.bjpowernode.springcloud.model.Goods;

import java.util.List;

public interface GoodsService{

    public List<Goods> getAllGoods();

    public Goods getGoodsById(Integer goodsId);
}