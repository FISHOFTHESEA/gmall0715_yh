package com.atguigu.gmall0715.manage.mapper;

import com.atguigu.gmall0715.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    /**
     * 通过spuid查找出对应的消费属性列表
     * @param spuId
     * @return
     */
    List<Map> getSaleAttrValuesBySpu(String spuId);
}
