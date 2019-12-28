package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class ManagerController {

    @Reference
    private ManageService manageService;

    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){

        return manageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    //localhost:8082/getCatalog3?catalog2Id=47
    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    //http://localhost:8082/attrInfoList?catalog3Id=406
    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    //localhost:8082/saveAttrInfo
    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
         manageService.saveAttrInfo(baseAttrInfo);
    }

    //localhost:8082/getAttrValueList?attrId=100
    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList( String attrId){
        //return manageService.getAttrValueList(attrId);

        //先查询baseAttrInfo
        // select * from baseAttrInfo where id = attrId
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        if(baseAttrInfo == null){
            return null;
        }

        return baseAttrInfo.getAttrValueList();

    }




}
