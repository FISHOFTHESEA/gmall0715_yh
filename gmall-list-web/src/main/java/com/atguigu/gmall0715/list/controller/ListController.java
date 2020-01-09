package com.atguigu.gmall0715.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.service.ListService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request){
        SkuLsResult skuLsResult = listService.search(skuLsParams);

        //从结果中取出平台属性值列表
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);

        //已选的属性值列表
        List<BaseAttrValue> baseAttrValueList = new ArrayList<>();
        String urlParam = makeUrlParam(skuLsParams);

        //遍历
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue:attrValueList){
                if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                    for (String valueId : skuLsParams.getValueId()){
                        //选中的属性值和查询结果的属性值
                        iterator.remove();
                        //构造面包屑列表
                        BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                        baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                        //去掉重复数据
                        String makeUrlParam = makeUrlParam(skuLsParams,valueId);
                        baseAttrValueSelected.setUrlParam(makeUrlParam);
                        baseAttrValueList.add(baseAttrValueSelected);
                    }
                }
            }
        }
        //设置每页显示的条数
        skuLsParams.setPageSize(2);

        //添加分页
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());
        //保存面包屑清单
        request.setAttribute("baseAttrValuesList",baseAttrValueList);
        request.setAttribute("keyword",skuLsParams.getKeyword());
        request.setAttribute("urlParam",urlParam);
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);
        //获取sku属性值列表
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        request.setAttribute("skuLsInfoList",skuLsInfoList);

        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds) {
        String urlParam="";
        List<String> paramList = new ArrayList<>();
        if (skuLsParams.getKeyword()!=null){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        if (skuLsParams.getCatalog3Id()!=null){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        //构造属性参数
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (int i = 0;i<skuLsParams.getValueId().length;i++) {
                String valueId = skuLsParams.getValueId()[i];
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        //跳出代码，后面的参数则不会继续追加，后续代码不会执行
                        //不能写break；如果写break其他条件则无法拼接
                        continue;
                    }
                }
                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }

}
