package com.atguigu.gmall0715.cart.servuce.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0715.cart.util.CartConst;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.service.CartInfoService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    private RedisUtil redisUtil;

    @Reference
    private ManageService manageService;

    @Autowired
    private CartInfoMapper cartInfoMapper;



    @Override
    public void addToCart(String skuId, String userId, Integer skuNam) {
        /**
         * 查看数据库中是否有该商品
         * select * from cartInfo where userid = ? and skuid = ?
         * true 数量相加 update
         * false 直接添加
         *
         * 放入redis
         */

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        // 调用查询数据库并加入缓存
        if(!jedis.exists(cartKey)){
            loadCartCache(userId);
        }

        //添加数据库
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectByExample(example);
        CartInfo cartInfoExist = null;
        if (cartInfoList!=null && cartInfoList.size()>0){
            cartInfoExist = cartInfoList.get(0);
        }

        //说明该商品已经在数据库中存在
        if (cartInfoExist!=null){
            //数量更新
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNam);
            //初始化实时价格
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            //更新数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);

            //更新redis  cartInfoExist
        }else{
            //商品在数据库中不存在
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();

            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNam);
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            //添加到数据库
            cartInfoMapper.insertSelective(cartInfo);
            //更新redis
            cartInfoExist = cartInfo;
        }
        String cartInfoJson = JSON.toJSONString(cartInfoExist);
        //更新redis放在最后
        jedis.hset(cartKey,skuId,cartInfoJson);
        setCartkeyExpireTime(userId,jedis,cartKey);
        //关闭redis
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        /**
         * 1、获取redis中的购物车数据
         * 2、如果redis没有，从数据库中获取并放入缓存
         */
        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();

        //定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        List<String> stringList = jedis.hvals(cartKey);

        if (stringList != null && stringList.size()>0){
            for (String cartJson : stringList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            cartInfoList.sort( new Comparator<CartInfo>(){
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });

            return cartInfoList;
        }else{
            //走数据库 ----放入redis
            cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartTempList, String userId) {
         /*
    demo1:
        登录：
            37 1
            38 1
        未登录：
            37 1
            38 1
            39 1
        合并之后的数据
            37 2
            38 2
            39 1
     demo2:
         未登录：
            37 1
            38 1
            39 1
            40  1
          合并之后的数据
            37 1
            38 1
            39 1
            40  1
     */

        //获取到登录时购物车数据
        List<CartInfo> cartInfoListLogin = cartInfoMapper.selectCartListWithCurPrice(userId);
        //判断登录时购物车数据是否为空？
        if (cartInfoListLogin != null && cartInfoListLogin.size()>0){
            for (CartInfo cartInfoNoLogin : cartTempList) {
                //声明一个boolean类型变量
                boolean isMatch = false;
                //如果数据库中没有数据
                for (CartInfo cartInfoLogin : cartInfoListLogin) {
                    //操作商品可能会发生异常
                    if (cartInfoNoLogin.getSkuId().equals(cartInfoLogin.getSkuId())){
                        //数量相加
                        cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum()+cartInfoNoLogin.getSkuNum());
                        //更新数据库
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoLogin);
                        isMatch = true;
                    }
                }
                //表示登录的购物车数据与未登录购物车数据没有匹配上
                if (!isMatch){
                    //直接添加数据库
                    cartInfoNoLogin.setId(null);
                    cartInfoNoLogin.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoNoLogin);
                }
            }
        }else {
            //数据库为空！直接添加到数据库
            for (CartInfo cartInfo : cartTempList) {
                cartInfo.setId(null);
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
            }
        }
        //汇总
        List<CartInfo> cartInfoList = loadCartCache(userId);

        //重新在数据库中查询并返回数据
        //cartInfoList数据中数量合并之后的集合
        //合并：选中状态为1
        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo cartInfo : cartTempList) {
                //sku id 相同
                if (cartInfoDB.getSkuId().equals(cartInfo.getSkuId())){
                    //合并未登陆选中的数据
                    //如果数据库中为1，未登录中也未1，不用修改
                    if ("1".equals(cartInfo.getIsChecked())){
                        if (!"1".equals(cartInfoDB.getIsChecked())){
                            //修改数据库字段为1
                            cartInfoDB.setIsChecked("1");
                            //修改商品状态为被选中
                            checkCart(cartInfo.getIsChecked(),cartInfo.getSkuId(),userId);
                        }
                    }
                }
            }
        }

        return cartInfoList;
    }

    @Override
    public void deleteCartList(String userTempId) {
        //先删除表中数据
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userTempId);
        cartInfoMapper.deleteByExample(example);

        //删除缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX+userTempId+CartConst.USER_CART_KEY_SUFFIX;
        jedis.del(cartKey);

        jedis.close();
    }

    @Override
    public void checkCart(String isChecked, String skuId, String userId) {
        /**
         * 1、修改缓存
         * 2、修改数据库
         */
        //修改数据update cartInfo set id_checked = ? where userid = ? and skiud = ?
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        System.out.println("修改数据------------------------");
        cartInfoMapper.updateByExampleSelective(cartInfo,example);

        //先删除缓存，再放入缓存
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //删除数据
        jedis.hdel(cartKey,skuId);

        //放入缓存
        //select * from cartInfo where userId = ? and skuId = ?
        List<CartInfo> cartInfoList = cartInfoMapper.selectByExample(example);
        //获取集合数据第一条数据
        if (cartInfoList!=null && cartInfoList.size()>0){
            CartInfo cartInfoQuery = cartInfoList.get(0);
            //数据初始化实时价格
            cartInfoQuery.setSkuPrice(cartInfoQuery.getCartPrice());
            jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfoQuery));
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //获取数据
        List<String> cartList = jedis.hvals(cartKey);
        if (cartList != null && cartList.size()>0){
            //遍历将条件符合的sku放入订单
            for (String cartJson : cartList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                if ("1".equals(cartInfo.getIsChecked())){
                    cartInfoList.add(cartInfo);
                }
            }
        }
        jedis.close();
        return cartInfoList;
    }

    /**
     * 从数据库中获取数据并存入redis缓存
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        //使用实时价格：将skuInfo.price价格赋值cartInfo.skuPrice
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        if (cartInfoList!= null && cartInfoList.size()>0){
            return null;
        }
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        HashMap<String,String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(cartKey,map);
        setCartkeyExpireTime(userId,jedis,cartKey);

        jedis.close();
        return cartInfoList;
    }

    /**
     * 设置过期时间
     * @param userId
     * @param jedis
     * @param cartKey
     */
    private void setCartkeyExpireTime(String userId, Jedis jedis, String cartKey) {
        //根据user得过期时间设置
        //获取用户的过期时间 user:userId:info
        String userKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        //用户key存在，登录
        Long expireTime = null;
        if (jedis.exists(userKey)){
            //获取过期时间
            expireTime = jedis.ttl(userKey);
            //给购物车的key设置
            jedis.expire(cartKey,expireTime.intValue());
        }else{
            //给购物车的key设置
            jedis.expire(cartKey,7*24*3600);
        }
    }
}
