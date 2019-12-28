package com.atguigu.gmall0715.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.service.UserAddressService;
import com.atguigu.gmall0715.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

//    @Autowired
    @Reference
    private UserService UserService;

    //根据用户id查询收货地址列表
    @RequestMapping("trade")
    public List<UserAddress> trade(String userId){

        return UserService.findUserAddressByUserId(userId);
    }
}
