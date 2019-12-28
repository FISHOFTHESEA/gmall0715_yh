package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;

import java.util.List;
//业务层的接口
public interface UserService {

    /**
     * 查询所有数据
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户Id查询用户地址列表
     * @param userId
     * @return
     */
    List<UserAddress> findUserAddressByUserId(String userId);

}
