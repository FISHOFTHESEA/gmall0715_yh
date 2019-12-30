package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.bean.SpuImage;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    /**
     * http://localhost:8082/spuImageList?spuId=59
     * 根据spuid查询该spu图片列表
     */
    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId, SpuImage spuImage){
        return manageService.getSpuImageList(spuImage);
    }

    /**
     * http://localhost:8082/spuSaleAttrList?spuId=59
     * 根据spuid查询消费属性列表
     */
    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId){
        return manageService.getspuSaleAttrList(spuId);
    }

    /**
     * http://localhost:8082/saveSkuInfo
     * 保存sku
     */
    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
    }

}
