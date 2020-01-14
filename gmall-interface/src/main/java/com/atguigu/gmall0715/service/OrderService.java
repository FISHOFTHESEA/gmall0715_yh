package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

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

    /**
     * 通过订单id查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 修改订单状态
     * @param orderId
     * @param paid
     */
    void updateOrderStatus(String orderId, ProcessStatus paid);

    /**
     * 根据orderid减少对应商品的库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 订单拆分
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);

    /**
     * 初始化订单
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);
}
