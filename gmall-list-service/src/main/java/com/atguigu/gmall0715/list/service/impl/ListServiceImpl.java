package com.atguigu.gmall0715.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.SkuLsInfo;
import com.atguigu.gmall0715.bean.SkuLsParams;
import com.atguigu.gmall0715.bean.SkuLsResult;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";



    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        //保存数据
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
            DocumentResult documentResult = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        String query = makeQueryStringForSearch(skuLsParams);

        Search build = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams,searchResult);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        int timesToEs = 10;
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);
        if (hotScore%timesToEs == 0){
            updateHotScore(skuId,Math.round(hotScore));
        }
    }

    private void updateHotScore(String skuId, long hotScore) {
        String updateJson ="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update update = new Update.Builder(updateJson).index("gmall").type("SkuInfo").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {
        SkuLsResult skuLsResult = new SkuLsResult();
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>(skuLsParams.getPageSize());

        //获取sku列表
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo,Void>hit:hits) {
            SkuLsInfo skuLsInfo = hit.source;
            if (hit.highlight!=null && hit.highlight.size()>0){
                List<String> lightList = hit.highlight.get("skuName");
                //把带有高亮标签的字符替换为skuName
                String skuNameHI = lightList.get(0);
                skuLsInfo.setSkuName(skuNameHI);
            }
            skuLsInfoList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        skuLsResult.setTotal(searchResult.getTotal());

        //取记录个数并计算出总页数
        long totalPage =
                (searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);

        //取出涉及的属性值id
        ArrayList<String> attrValueIdList = new ArrayList<>();
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        if (groupby_attr!=null){
            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry bucket:buckets){
                attrValueIdList.add(bucket.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);
        }
        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //创建查询build
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (skuLsParams.getKeyword()!=null){
            MatchQueryBuilder match = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(match);
            //设置高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //设置高亮字段
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            //将高亮结果放入查询器中
            searchSourceBuilder.highlight(highlightBuilder);

        }
        //设置三级分类
        if (skuLsParams.getCatalog3Id()!=null){
            TermQueryBuilder catalog3Id = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(catalog3Id);
        }
        //设置属性值
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (int i = 0;i<skuLsParams.getValueId().length;i++){
                String valueId = skuLsParams.getValueId()[i];
                TermQueryBuilder valueIds = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(valueIds);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //设置分页
        int form = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(form);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //设置按照热度排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //设置聚合
        TermsBuilder attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(attr);

        String query = searchSourceBuilder.toString();
        System.out.println("query="+query);

        return query;
    }



}
