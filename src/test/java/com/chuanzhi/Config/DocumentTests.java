package com.chuanzhi.Config;

import com.alibaba.fastjson.JSON;
import com.chuanzhi.Pojo.Article;
import com.chuanzhi.Pojo.ArticleDocument;
import com.chuanzhi.Service.ArticleService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class DocumentTests {
    @Autowired
    private ArticleService articleService;

    private RestHighLevelClient client;

    //添加文档
    @Test
    void testAddDocument() throws IOException {
        // 1.根据id查询酒店数据
        Article article = articleService.getById(4);
        ArticleDocument articleDocument=new ArticleDocument(article);
        // 3.将HotelDoc转json
        String json = JSON.toJSONString(articleDocument);

        // 1.准备Request对象
        IndexRequest request = new IndexRequest("article").id(article.getId().toString());
        // 2.准备Json文档
        request.source(json, XContentType.JSON);
        // 3.发送请求
        try {
            client.index(request, RequestOptions.DEFAULT);
        }catch (IOException e){
            if (!(e.getMessage().contains("200"))){
                System.out.println("新增文档出错");
            }
            else{
                System.out.println("新增文档成功！");
            }
        }
    }

    //查询文档
    @Test
    void testGetDocumentById() throws IOException {
        // 1.准备Request
        GetRequest request = new GetRequest("article", "1");
        // 2.发送请求，得到响应
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 3.解析响应结果
        String json = response.getSourceAsString();

        ArticleDocument articleDocument = JSON.parseObject(json, ArticleDocument.class);
        System.out.println(articleDocument);
    }


    //删除文档
    @Test
    void testDeleteDocument() throws IOException {
        // 1.准备Request
        DeleteRequest request = new DeleteRequest("article", "2");
        // 2.发送请求
        try {
            client.delete(request, RequestOptions.DEFAULT);
        }catch (IOException e){
            if (!(e.getMessage().contains("200"))){
                System.out.println("删除文档出错");
            }
            else {
                System.out.println("删除文档成功！");
            }
        }

    }

    //修改文档
    @Test
    void testUpdateDocument() throws IOException {
        // 1.准备Request
        UpdateRequest request = new UpdateRequest("article", "2");
        // 2.准备请求参数
        request.doc(
                "name", "红楼梦",
                "info", "林黛玉" ,
                "writer", "罗贯中"
        );
        // 3.发送请求
        try {
            client.update(request, RequestOptions.DEFAULT);
        }catch (IOException e){
            if (!(e.getMessage().contains("200"))){
                System.out.println("修改文档出错");
            }
            else {
                System.out.println("修改文档成功！");
            }
        }
    }


    //批量添加文档
    @Test
    void testBulkRequest() throws IOException {
        // 批量查询文章数据
        List<Article> articles = articleService.list();

        // 1.创建Request
        BulkRequest request = new BulkRequest();
        // 2.准备参数，添加多个新增的Request
        for (Article article : articles) {
            // 2.1.转换为文档类型HotelDoc
            ArticleDocument articleDocument=new ArticleDocument(article);
            // 2.2.创建新增文档的Request对象
            request.add(new IndexRequest("article")
                    .id(articleDocument.getId().toString())
                    .source(JSON.toJSONString(articleDocument), XContentType.JSON));
        }
        // 3.发送请求
        try {
            client.bulk(request, RequestOptions.DEFAULT);
        }catch (IOException e){
            if (!(e.getMessage().contains("200"))){
                System.out.println("新增文档出错");
            }
            else {
                System.out.println("新增文档成功!");
            }
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
