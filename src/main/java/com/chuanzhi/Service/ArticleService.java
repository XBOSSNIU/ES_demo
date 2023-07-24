package com.chuanzhi.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chuanzhi.Pojo.Article;
import com.chuanzhi.Pojo.PageResult;
import com.chuanzhi.Pojo.RequestParams;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ArticleService extends IService<Article> {
    PageResult search(RequestParams params);

    List<String> getSuggestions(String prefix);
}
