package com.atguigu.gmall0715.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    /**
     * 获取到配置文件中的host，port，timeOut等参数
     * 将RedisUtil放入到spring容器中管理
     */
    //disabled表示如果配置文件中没有获取到host，则表示默认值为disabled
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;


    @Value("${spring.redis.timeOut:10000}")
    private int timeOut;

    /*
    <bean id = "redisUtil" class="com.atguigu.gmall0311.config.RedisUtil">
    </bean>
     */

    @Bean
    public RedisUtil getRedisUtil(){
        // 表示配置文件中没有host
        if ("disabled".equals(host)){
            return null;
        }
        //初始化连接工厂
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initJedisPool(host,port,timeOut);
        return redisUtil;
    }

}
