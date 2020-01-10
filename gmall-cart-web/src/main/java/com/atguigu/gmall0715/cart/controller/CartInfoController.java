package com.atguigu.gmall0715.cart.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.config.utils.CookieUtil;
import com.atguigu.gmall0715.service.CartInfoService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartInfoController {

    @Reference
    private CartInfoService cartInfoService;

    @Reference
    private ManageService manageService;


    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");

        //判断用户是否为第一次购买
        if (userId == null){
            //可能存在cookie中
            userId = CookieUtil.getCookieValue(request,"my-userId",false);
            //如果cookie中没有userID则新建一个存入cookie中
            if (userId ==null){
                userId = UUID.randomUUID().toString().replace("-","");
                CookieUtil.setCookie(request,response,"my-userId",userId,60*60*24*7,false);
            }
        }

        cartInfoService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        //保存skuinfo对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        //保存添加的数量
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        //从作用域中获取userId
        String userId = (String) request.getAttribute("userId");
        //声明购物车集合列表
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (userId == null){
            //从未登陆的购物车获取数据
            //获取cookie中的my-userId
            String userTempId = CookieUtil.getCookieValue(request,"my-userId",false);
            //调用服务层的方法获取缓存中的数据
            if (!StringUtils.isEmpty(userTempId)){
                //从缓存中获取购物车数据列表
                cartInfoList = cartInfoService.getCartList(userTempId);
            }
        }else{
            //从缓存中获取购物车数据列表
            //查询未登陆是否有购物车数据
            //从cookie中获取临时的userid
            String userTempId = CookieUtil.getCookieValue(request,"my-userId",false);
            //调用服务层的方法获取缓存中的数据
            //合并购物车{合并未登录购物车数据}
            //声明一个集合来存储未登陆数据
            List<CartInfo> cartTempList = new ArrayList<>();
            if (!StringUtils.isEmpty(userTempId)){
                //从缓存中获取购物车数据列表
                cartTempList = cartInfoService.getCartList(userTempId);
                if (cartTempList!= null && cartTempList.size()>0){
                    //合并购物车：cartTempList未登陆购物车,根据userid查询登陆购物车数据
                    cartInfoList = cartInfoService.mergeToCartList(cartTempList,userId);
                    //删除未登录购物车数据
                    cartInfoService.deleteCartList(userTempId);
                }
            }
            //cartTemList == null || cartTemList.size() == 0
            if (userTempId == null || (cartTempList == null || cartTempList.size()==0)){
                //说明未登录没有数据，直接获取数据库
                cartInfoList = cartInfoService.getCartList(userId);
            }
        }
        //保存到作用域
        request.setAttribute("cartInfoList",cartInfoList);
        return "cartList";

    }

    //获取前台传入的数据
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        //调用服务层,从页面获取参数
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        //获取用户id
        String userId = (String)request.getAttribute("userId");

        //判断用户状态
        if (userId == null){
            //登录状态
            userId = CookieUtil.getCookieValue(request,"my-userId",false);
        }
        cartInfoService.checkCart(isChecked,skuId,userId);
    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        //从作用域中获取userid
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartTempList = null;
        //获取cookie中的my-userId
        String userTempId = CookieUtil.getCookieValue(request,"my-userId",false);
        if (userTempId != null){
            cartTempList = cartInfoService.getCartList(userTempId);
            if (cartTempList != null && cartTempList.size()>0){
                //合并勾选状态
                List<CartInfo> cartInfoList = cartInfoService.mergeToCartList(cartTempList, userId);
                //删除
                cartInfoService.deleteCartList(userTempId);
            }
        }
        return "redirect://trade.gmall.com/trade";
    }

}
