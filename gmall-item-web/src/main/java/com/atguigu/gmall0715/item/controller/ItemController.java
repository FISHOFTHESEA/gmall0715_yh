package com.atguigu.gmall0715.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.service.ListService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @LoginRequire
    @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable(value = "skuId") String skuId, HttpServletRequest request){
        //存储基本的skuinfo信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        //存储spu，sku数据
        List<SpuSaleAttr> saleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        request.setAttribute("saleAttrList",saleAttrList);
        //实现点击分类切换页面
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        request.setAttribute("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
        //异步调用
        listService.incrHotScore(skuId);
        return "item";
    }

}
