package com.atguigu.gmall0715.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.OrderDetail;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.enums.OrderStatus;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.service.CartInfoService;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@RestController
@Controller
public class OrderController {

//    @Autowired
    @Reference
    private UserService userService;

    @Reference
    private CartInfoService cartInfoService;

    @Reference
    private OrderService orderService;


    /**
     * 生成订单
     * @param request
     * @return
     */
    //根据用户id查询收货地址列表
    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        //得到选中的购物车列表
        List<CartInfo> cartList = cartInfoService.getCartCheckedList(userId);
        //收获人地址
        List<UserAddress> userAddressByUserId = userService.findUserAddressByUserId(userId);
        request.setAttribute("userAddressList",userAddressByUserId);
        //订单信息集合
        List<OrderDetail> orderDetailList = new ArrayList<>(cartList.size());
        for (CartInfo cartInfo : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        //给订单赋一个唯一流水号，防止用户恶意重复提交
        //获取TradeCode号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeCode",tradeNo);

        return "trade";
        //return UserService.findUserAddressByUserId(userId);
    }

    /**
     * 提交订单
     * http://trade.gmall.com/submitOrder
     */
    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        //检查tradeCode
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId,tradeNo);
        if (!flag){
            request.setAttribute("errMsg","该页面已过期，请重新结算");
            return "tradeFail";
        }
        //初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        //校验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            //从订单中根据skuid去查询sku数量
            boolean result = orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            if (!result){
                request.setAttribute("errMsg","卖光了，客官下次再来！");
            }
        }


        //保存
        String orderId = orderService.saveOrder(orderInfo);
        //删除tradeNo
        orderService.delTradeNo(userId);
        //重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        //定义订单集合
        List<OrderInfo> subOrderInfoList = orderService.splitOrder(orderId,wareSkuMap);
        List<Map> wareMapList = new ArrayList<>();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            wareMapList.add(map);
        }
        return JSON.toJSONString(wareMapList);
    }

}
