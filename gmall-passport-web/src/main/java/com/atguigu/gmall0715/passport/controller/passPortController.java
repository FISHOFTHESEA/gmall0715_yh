package com.atguigu.gmall0715.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.passport.utils.JwtUtil;
import com.atguigu.gmall0715.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class passPortController {

    @Value("${token.key}")
    String signKey;

    @Reference
    private UserService userService;


    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");

        //传到页面
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request,UserInfo userInfo){
        // 调用服务层
        UserInfo info = userService.login(userInfo);
        if (info != null) {
            // data --- token
            // 参数
//            String key = "atguigu";
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            // 服务的Ip 地址 配置nginx 服务器代理
            String salt = request.getHeader("X-forwarded-for");
//            String salt = "192.168.67.123";
            String token = JwtUtil.encode(signKey, map, salt);
            return token;
        }
        return "fail";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");
        //检查token
        Map<String, Object> checkToken = JwtUtil.decode(token, signKey, salt);
        if (checkToken != null){
            //检查redis信息
            String userId = (String)checkToken.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if (userInfo != null){
                return "success";
            }
        }
        return "fail";
    }

}
