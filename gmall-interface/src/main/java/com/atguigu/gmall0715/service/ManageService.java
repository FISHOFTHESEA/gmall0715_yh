package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.*;

import java.util.List;

public interface ManageService {
    /**
     * 查询一级分类数据
     *
     * @return
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 查询二级分类数据
     *
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 查询三级分类数据
     *
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 通过三级分类Id查询
     *
     * @return
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存平台属性，平台属性值
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 修改时回显数据
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 通过attrId去查询baseAttrInfo
     * @param attrId
     * @return
     */
    BaseAttrInfo getBaseAttrInfo(String attrId);

    /**
     * 通过三级分类Id查询
     * @param catalog3Id
     * @return
     */
    List<SpuInfo> getSpuInfoList(String catalog3Id);
}
