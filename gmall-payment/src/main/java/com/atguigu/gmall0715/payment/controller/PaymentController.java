package com.atguigu.gmall0715.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.PaymentInfo;
import com.atguigu.gmall0715.bean.enums.PaymentStatus;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.payment.config.AlipayConfig;
import com.atguigu.gmall0715.payment.config.StreamUtil;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.service.PaymentService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {



    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @Reference
    private AlipayClient alipayClient;



    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;

    @RequestMapping("index")
    @LoginRequire
    public String index(HttpServletRequest request){
        //获取订单id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("orderId",orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        //获取订单id
        String orderId = request.getParameter("orderId");
        //获取订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("---------------------");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        //保存信息
        paymentService.savePaymentInfo(paymentInfo);

        //支付宝参数
        //创建api对应的request
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //在公共参数中设置回调和通知地址
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        //声明一个map
        Map<String,Object> bizContnetMap=new HashMap<>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
        // 将map变成json
        String Json = JSON.toJSONString(bizContnetMap);
        alipayTradePagePayRequest.setBizContent(Json);
        String form="";
        try {
            form = alipayClient.pageExecute(alipayTradePagePayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        return form;

    }

    @RequestMapping("alipay/callback/return")
    public String callbackReturn(){
        return "redirect:"+ AlipayConfig.return_order_url;
    }


    @RequestMapping("alipay/callback/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8",AlipayConfig.sign_type);
        if (!flag){
            return "fial";
        }
        // 判断结束
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            // 查单据是否处理
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(paymentInfo);
            if (paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID || paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else {
                // 修改
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                // 设置状态
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 设置创建时间
                paymentInfoUpd.setCallbackTime(new Date());
                // 设置内容
                paymentInfoUpd.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                return "success";
            }
        }
        return  "fail";
    }

    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        boolean flag = paymentService.refund(orderId);
        System.out.println("flag:"+flag);
        return flag+"";
    }


    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(String orderId){
        //做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面
        //调用服务层数据
        //第一个参数是订单id，第二个参数是多少钱，单位是分
        Map map = paymentService.createNative(orderId+"","1");
        System.out.println(map.get("code_url"));

        return map;
    }

    @RequestMapping("wx/callback/notify")
    public String wxNotify(HttpServletRequest request,HttpServletResponse response) throws Exception {
        //0
        ServletInputStream inputStream = request.getInputStream();
        String xmlString = StreamUtil.inputStream2String(inputStream, "utf-8");

        //1
        if (WXPayUtil.isSignatureValid(xmlString,partnerkey)){
            //判断状态
            Map<String,String> paramMap = WXPayUtil.xmlToMap(xmlString);
            String result_code = paramMap.get("result_code");
            if (result_code != null && result_code.equals("SUCCESS")){
                //更新支付状态， 发送消息给订单

                //准备返回值xml
                HashMap<String,String> returnMap = new HashMap<>();
                returnMap.put("return_code","SUCCESS");
                returnMap.put("return_msg","OK");

                String returnXml = WXPayUtil.mapToXml(returnMap);
                response.setContentType("text/xml");
                System.out.println("交易编号:"+paramMap.get("out_trade_no")+"支付成功");

                return returnXml;
            }else {
                System.out.println(paramMap.get("return_code")+"-------"+paramMap.get("return_msg"));
            }
        }
        return null;
    }


    //发送验证
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result")String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "sent payment result";
    }

    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(OrderInfo orderInfo){
        //根据orderId查询paymentInfo对象
        OrderInfo orderInfoQuery = orderService.getOrderInfo(orderInfo.getId());
        boolean res = paymentService.checkPayment(orderInfoQuery);
        return ""+res;
    }


}
