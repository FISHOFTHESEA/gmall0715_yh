package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    /**
     * 保存支付信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 获取支付信息
     * @param paymentInfo
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 修改支付订单信息
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 退款
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    /**
     * 微信支付
     * @param orderId
     * @param total_fee
     * @return
     */
    Map createNative(String orderId, String total_fee);

    /**
     * 发送验证
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    /**
     * 检查支付状态
     * @param orderInfoQuery
     * @return
     */
    boolean checkPayment(OrderInfo orderInfoQuery);

    /**
     * 关闭订单
     */
    void closeOrderInfo(String outTradeNo,int delaySec);
}
