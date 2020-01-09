package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.*;

import java.util.List;
import java.util.Map;

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
    List<BaseAttrInfo> getAttrList(BaseAttrInfo baseAttrInfo);

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

    /**
     * 查询商品销售属性
     * @return
     */
    List<BaseSaleAttr> selectBaseSaleAttrList();

    /**
     * 保存商品spu信息
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuid查询图片列表
     * @param
     * @return
     */
    List<SpuImage> getSpuImageList(SpuImage spuImage);

    /**
     * 根据spuid查询消费属性列表
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getspuSaleAttrList(String spuId);

    /**
     * 在sku里查询三级分类时查询属性列表
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存商品的sku信息
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据skuid查询对应的sku信息
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuinfo查出该spu下对应的skuinfo列表
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 通过spuid查找出对应sku的id列表，实现点击分类跳转不同页面
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(String spuId);

    /**
     * 通过查询结果中的属性id列表查询对应的属性
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);

}
