package com.atguigu.gmall0715.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.service.UserService;
import com.atguigu.gmall0715.user.mapper.UserAddressMapper;
import com.atguigu.gmall0715.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;
@Service
public class UserServiceImpl implements UserService{

    @Autowired(required = false)
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;


    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;



    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddressByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if (info != null){
            //获得到redis,将用户存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+
                            info.getId()+
                            userinfoKey_suffix,
                    userKey_timeOut,
                    JSON.toJSONString(info));
            jedis.close();
            return info;
        }

        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        //去缓存中查询是否有该用户信息
        Jedis jedis = redisUtil.getJedis();
        String key = userKey_prefix+userId+userinfoKey_suffix;
        String userJson = jedis.get(key);
        //延长过期时间
        jedis.expire(key,userKey_timeOut);
        if (userJson != null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }
}
