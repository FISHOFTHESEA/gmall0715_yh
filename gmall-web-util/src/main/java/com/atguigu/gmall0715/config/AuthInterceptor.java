package com.atguigu.gmall0715.config;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.config.utils.CookieUtil;
import com.atguigu.gmall0715.config.utils.WebConst;
import com.atguigu.gmall0715.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter("newToken");
        //把token保存到cookie中
        if (token != null){
            CookieUtil.setCookie(request,response,"token",token, WebConst.COOKIE_MAXAGE,false);
        }
        //当用户登录成功之后，那么用户是否可以继续访问其他服务
        if (token == null){
            token = CookieUtil.getCookieValue(request,"token",false);
        }
        //当token真正不为空的时候，解密用户昵称
        if (token != null){
            //解密token
            Map map = getUserMapByToken(token);
            String nickName = (String)map.get("nickName");
            //保存到作用域
            request.setAttribute("nickName",nickName);
        }
        //获取用户访问的控制器上是否有注解@LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        //获取方法上的注解
        LoginRequire loginRequireAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //有注解
        if (loginRequireAnnotation != null){
            //直接认证！用户是否登陆！
            String salt = request.getHeader("X-forwarded-for");
            //远程调用
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS+"?token="+token+"&salt="+salt);
            if ("success".equals(result)){
                Map map = getUserMapByToken(token);
                String userId = (String)map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else {
                //当LoginRequire的注解中的属性autoRedirect =true 时必须登录！
                if (loginRequireAnnotation.autoRedirect()){
                    //得到用户访问的url路径
                    //http://item.gmall.com/36.html
                    String requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL,"UTF-8");
                    //重定向
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    //拦截
                    return false;
                }
            }
        }

        return true;
    }

    private Map getUserMapByToken(String token) {
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map map = JSON.parseObject(tokenJson,Map.class);

        return map;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
