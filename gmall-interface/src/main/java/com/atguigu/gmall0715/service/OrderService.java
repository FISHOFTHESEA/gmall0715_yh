package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.OrderInfo;

public interface OrderService {
    /**
     * 保存订单信息
     * @param orderInfo
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 给订单生成一个唯一的流水号，以免用户恶意重复提交
     * 提交时验证流水号是否相同
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 检查订单的流水号，true提交订单，false报错
     * @param userId
     * @param tradeNo
     * @return
     */
    boolean checkTradeCode(String userId, String tradeNo);

    /**
     * 删除该订单的流水号
     * @param userId
     */
    void delTradeNo(String userId);

    /**
     * 校验库存是否足够
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);
}
