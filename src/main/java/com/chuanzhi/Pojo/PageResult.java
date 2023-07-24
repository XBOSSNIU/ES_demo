package com.chuanzhi.Pojo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult {
    private Long total;
    private List<ArticleDocument> articles;
    public PageResult(){

    }

    public PageResult(Long total, List<ArticleDocument> articles){
        this.total=total;
        this.articles=articles;
    }

}
