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

    /**
     * 核对后台登录信息+用户登录信息
     * @param userInfo
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 检查redis缓存里是否存在用户信息，存在延长过期时间
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
