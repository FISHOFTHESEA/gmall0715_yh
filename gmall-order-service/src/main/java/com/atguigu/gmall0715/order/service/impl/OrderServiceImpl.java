package com.atguigu.gmall0715.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.OrderDetail;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0715.order.mapper.OrderMapper;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付编号
        String outTradeNo = "ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderMapper.insertSelective(orderInfo);

        //插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        //为了跳转到支付页面使用，支付会根据订单id进行支付
        String orderId = orderInfo.getId();

        return orderId;
    }

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode != null && tradeCode.equals(tradeNo)){
            return true;
        }
        return false;
    }

    @Override
    public void delTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey = "user"+userId+":tradeCode";

        String tradeCode = jedis.get(tradeNoKey);

        //jedis.del(tradeNoKey)
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        jedis.eval(script,Collections.singletonList(tradeNoKey),Collections.singletonList(tradeCode));

        jedis.close();
    }

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return true;
        }
        return false;
    }
}
