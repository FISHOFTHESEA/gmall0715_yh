package com.atguigu.gmall0715.list;


import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	private JestClient jestClient;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testES() throws IOException {
		/**
		 * 定义dsl语句
		 * {
		 		"query": {
		 			"match": {
					 "actorList.name": "张译"
					 }
		 		}
		 }
		 定义执行的动作
		 get movie_chn/movie/_search

		 执行

		 获取结果

		 */
		String query = "{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"actorList.name\": \"张译\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		//在哪个index，type中执行
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();
		//执行search查询动作
		SearchResult searchResult = jestClient.execute(search);
		//获取数据
		List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);

		for (SearchResult.Hit<Map,Void>hit:hits) {
			Map map = hit.source;
			System.out.println(map);
		}
	}

}
