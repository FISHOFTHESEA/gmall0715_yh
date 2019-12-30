package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ManagerController {

    @Reference
    private ManageService manageService;

    /**
     * 查询一级分类，页面加载时查询
     * @return
     */
    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){

        return manageService.getCatalog1();
    }

    /**
     * 根据一级分类id查询二级分类
     * @param catalog1Id
     * @return
     */
    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    /**
     * 根据二级分类id查询三级分类
     * @param catalog2Id
     * @return
     */
    //localhost:8082/getCatalog3?catalog2Id=47
    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    /**
     * 根据三级分类id查询对应的属性
     * @param catalog3Id
     * @return
     */
    //http://localhost:8082/attrInfoList?catalog3Id=406
    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id,BaseAttrInfo baseAttrInfo){
        return manageService.getAttrList(catalog3Id);
    }

    /**
     * 保存属性
     * @param baseAttrInfo
     */
    //localhost:8082/saveAttrInfo
    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }

    /**
     * 回显属性列表
     * @param attrId
     * @return
     */
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

    /**
     * http://localhost:8082/baseSaleAttrList
     * 查询消费属性列表
     */
    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.selectBaseSaleAttrList();
    }




}
