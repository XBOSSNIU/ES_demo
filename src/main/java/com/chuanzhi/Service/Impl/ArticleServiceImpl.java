package com.chuanzhi.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chuanzhi.Mapper.ArticleMapper;
import com.chuanzhi.Pojo.Article;
import com.chuanzhi.Pojo.ArticleDocument;
import com.chuanzhi.Pojo.PageResult;
import com.chuanzhi.Pojo.RequestParams;
import com.chuanzhi.Service.ArticleService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper,Article> implements ArticleService {

    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public PageResult search(RequestParams params) {
        try {
            // 1.准备Request
            SearchRequest request = new SearchRequest("article");
            // 2.准备DSL
            // 2.1.query
            buildBasicQuery(params,request);

            // 2.2.分页
            int page = params.getPage();
            int size = params.getSize();
            request.source().from((page - 1) * size).size(size);

            // 3.发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 4.解析响应
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        try {
            SearchRequest request = new SearchRequest("article");

            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "suggestions",
                    SuggestBuilders.completionSuggestion("suggestion")
                            .prefix(prefix)
                            .skipDuplicates(true)
                            .size(10)
            ));

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //解析结果
            Suggest suggest = response.getSuggest();
            CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
            List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
            List<String> list = new ArrayList<>(options.size());
            for (CompletionSuggestion.Entry.Option option : options) {
                String text = option.getText().toString();
                list.add(text);
            }
            return list;
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }



    private void buildBasicQuery(RequestParams params, SearchRequest request) {
        // 1.构建BooleanQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 2.关键字搜索
        String key = params.getKey();
        if (key == null || "".equals(key)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }
        // 7.放入source
        request.source().query(boolQuery);
    }

    PageResult handleResponse(SearchResponse response){
        //解析响应
        SearchHits searchHits=response.getHits();
        //4.1获得总条数
        long total = searchHits.getTotalHits().value;
        //4.2文档数组
        SearchHit[] hits=searchHits.getHits();
        //4.3遍历
        List<ArticleDocument> articles=new ArrayList<>();
        for (SearchHit hit:hits) {
            //获得文档source
            String json=hit.getSourceAsString();
            //反序列化
            ArticleDocument articleDocument = JSON.parseObject(json, ArticleDocument.class);
            articles.add(articleDocument);
        }
        return new PageResult(total,articles);
    }
}
