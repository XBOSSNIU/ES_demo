package com.chuanzhi.Config;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.chuanzhi.Pojo.Article;
import com.chuanzhi.Pojo.ArticleDocument;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class SearchTest {
    private RestHighLevelClient client;

    @Test
    void testMatchAll() throws IOException {
        //准备request
        SearchRequest request=new SearchRequest("article");
        //准备DSL
        request.source().query(QueryBuilders.matchAllQuery());
        //发请求
        SearchResponse response=client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    @Test
    void testMatch() throws IOException {
        //准备request
        SearchRequest request=new SearchRequest("article");
        //准备DSL
        request.source().query(QueryBuilders.matchQuery("all","清华"));
        //发请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        handleResponse(response);
    }

    //多项匹配
    @Test
    void testMultiMatch() throws IOException {
        //准备request
        SearchRequest request=new SearchRequest("article");
        //准备DSL
        request.source().query(QueryBuilders.multiMatchQuery("test","name","publisher"));
        //发请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        handleResponse(response);
    }

    //数据聚合
    @Test
    void testTerm() throws IOException {
        //准备request
        SearchRequest request=new SearchRequest("article");
        //准备DSL
        request.source().query(QueryBuilders.termQuery("writer","Xboss"));
        //发请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        handleResponse(response);
    }

    //范围匹配
    @Test
    void testRange() throws IOException {
        //准备request
        SearchRequest request=new SearchRequest("article");
        //准备DSL
        request.source().query(QueryBuilders.rangeQuery("price").gte(50).lte(200));
        //发请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        handleResponse(response);
    }

    //布尔查询
    @Test
    void testBool() throws IOException {
        //准备request
        SearchRequest request=new SearchRequest("article");
        //准备DSL
        BoolQueryBuilder boolQuery=new BoolQueryBuilder();
        boolQuery.must(QueryBuilders.termQuery("writer", "Xboss"));
        boolQuery.should(QueryBuilders.termQuery("publisher","新华出版社"));
        boolQuery.mustNot(QueryBuilders.rangeQuery("price").lte(50));
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(1000));
        request.source().query(boolQuery);
        //发请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        handleResponse(response);
    }


    //分页查询
    @Test
    void testPageAndSort() throws IOException {
        // 页码，每页大小
        int page = 1, size = 5;

        // 1.准备Request
        SearchRequest request = new SearchRequest("article");
        // 2.准备DSL
        // 2.1.query
        request.source().query(QueryBuilders.matchAllQuery());
        // 2.2.排序 sort
        request.source().sort("price", SortOrder.ASC);
        // 2.3.分页 from、size
        request.source().from((page - 1) * size).size(5);
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handleResponse(response);
    }

    //项聚合（遍历）
    @Test
    void testAggregation() throws IOException {
        SearchRequest request=new SearchRequest("article");
        request.source().size(0);
        request.source().aggregation(AggregationBuilders
                .terms("writerAgg")
                .field("writer")
                .size(10)
        );
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        //解析聚合结果
        Aggregations aggregations=response.getAggregations();
        //根据名称获取聚合结果
        Terms brandTerms=aggregations.get("writerAgg");
        //获得桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //遍历
        for (Terms.Bucket bucket : buckets) {
            String key=bucket.getKeyAsString();
            System.out.println(key);
        }

    }


    //自动补全
    @Test
    void testSuggest() throws IOException {
        SearchRequest request=new SearchRequest("article");

        request.source().suggest(new SuggestBuilder().addSuggestion(
                "suggestions",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix("1")
                        .skipDuplicates(true)
                        .size(10)
        ));

        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        //解析结果
        Suggest suggest=response.getSuggest();
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        for (CompletionSuggestion.Entry.Option option : options) {
            String text=option.getText().toString();
            System.out.println(text);
        }
    }

    void handleResponse(SearchResponse response){
        //解析响应
        SearchHits searchHits=response.getHits();
        //4.1获得总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到"+total+"条数据");
        //4.2文档数组
        SearchHit[] hits=searchHits.getHits();
        //4.3遍历
        for (SearchHit hit:hits) {
            //获得文档source
            String json=hit.getSourceAsString();
            //反序列化
            ArticleDocument articleDocument = JSON.parseObject(json, ArticleDocument.class);
            // 获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                // 根据字段名获取高亮结果
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null) {
                    // 获取高亮值
                    String name = highlightField.getFragments()[0].string();
                    // 覆盖非高亮结果
                    articleDocument.setName(name);
                }
            }
            System.out.println("article="+articleDocument);
        }
    }

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://47.115.216.42:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
}
