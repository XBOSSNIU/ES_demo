package com.chuanzhi.Pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
public class ArticleDocument {
    private Long id;
    private String writer;
    private String name;
    private  String type;
    private String info;
    private Long price;
    private String publisher;
    private List<String> suggestion;
    public ArticleDocument(Article article){
        this.id=article.getId();
        this.writer=article.getWriter();
        this.name=article.getName();
        this.type=article.getType();
        this.info=article.getInfo();
        this.price=article.getPrice();
        this.publisher=article.getPublisher();
        this.suggestion = Arrays.asList(this.writer, this.name,this.type);
    }
}
