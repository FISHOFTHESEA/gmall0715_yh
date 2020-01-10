package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.CartInfo;

import java.util.List;

public interface CartInfoService {
    /**
     * 将传入的信息保存到购物车
     * @param skuId
     * @param userId
     * @param skuNam
     */
    void addToCart(String skuId, String userId, Integer skuNam);

    /**
     * 根据userid获取购物车列表
     * @param userId
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车，将根据userid查出来的购物车和现有的购物车合并
     * @param cartTempList
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartTempList, String userId);

    /**
     * 根据传入的id删除购物车数据
     * @param userTempId
     */
    void deleteCartList(String userTempId);

    /**
     * 检查购物车保存选中状态，不必走数据库，保存到缓存即可
     * @param isChecked
     * @param skuId
     * @param userId
     */
    void checkCart(String isChecked, String skuId, String userId);

    /**
     * 根据userid获取购物车列表，结算已选中的
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
