package com.atguigu.gmall0715.cart.mapper;

import com.atguigu.gmall0715.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    /**
     * 通过用户id获取该用户的购物车列表信息
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
