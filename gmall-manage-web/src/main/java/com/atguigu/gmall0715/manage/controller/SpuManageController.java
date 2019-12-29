package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.SpuImage;
import com.atguigu.gmall0715.bean.SpuInfo;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.bean.SpuSaleAttrValue;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuList")
    public List<SpuInfo> getSpuInfoList (String catalog3Id){

        return manageService.getSpuInfoList(catalog3Id);
    }

    /**
     * http://localhost:8082/saveSpuInfo
     * 保存spu信息
     */
    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
    }

    /**
     * http://localhost:8082/spuImageList?spuId=59
     * 根据spuid查询该spu图片列表
     */
    @GetMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        return manageService.getSpuImageList(spuId);
    }

    /**
     * http://localhost:8082/spuSaleAttrList?spuId=59
     * 根据spuid查询消费属性列表
     */
    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId){
        return manageService.getspuSaleAttrList(spuId);
    }



}
