package com.atguigu.gmall0715.manage.mapper;

import com.atguigu.gmall0715.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    /**
     * 根据三级id查询属性列表
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> selectBaseAttrInfoListByCatalog3Id(String catalog3Id);
}
