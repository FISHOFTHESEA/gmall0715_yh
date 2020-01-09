package com.atguigu.gmall0715.passport;


import com.atguigu.gmall0715.passport.utils.JwtUtil;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {
		String key = "atguigu";
		String ip = "192.168.3.215";
		Map map = new HashMap();
		map.put("userId","101");
		map.put("nickName","marry");
		String token = JwtUtil.encode(key, map, ip);
		Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.3.215");
		System.out.println("token======"+token);
		System.out.println("decode======"+decode);
	}

}
