package com.chuanzhi.Config;

import com.chuanzhi.Mapper.ArticleMapper;
import com.chuanzhi.Pojo.Article;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static com.chuanzhi.Constants.ArticleConstants.MAPPING_TEMPLATE;

@SpringBootTest
class IndexTests {
    @Autowired
    ArticleMapper articleMapper;

    private RestHighLevelClient client;

    @Test
    void contextLoads() {
    }

    @Test
    void insertTest() {
        Article article = new Article();
        article.setName("活着");
        article.setWriter("余华");
        article.setInfo("略");
        articleMapper.insert(article);
    }


    @Test
    void testInit() {
        System.out.println(client);
    }

    //创建索引
    @Test
    void creat() throws IOException {
        //1.创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("article");

        //2.准备请求的参数：DSL语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);

        //3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    //删除索引
    @Test
    void testDeleteHotelIndex() throws IOException {
        // 1.创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("article");
        // 2.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }


    //测试索引是否存在
    @Test
    void testExistsHotelIndex() throws IOException {
        // 1.创建Request对象
        GetIndexRequest request = new GetIndexRequest("article");
        // 2.发送请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        // 3.输出
        System.err.println(exists ? "索引库已经存在！" : "索引库不存在！");
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
