package com.chuanzhi.Controller;

import com.chuanzhi.Pojo.PageResult;
import com.chuanzhi.Pojo.RequestParams;
import com.chuanzhi.Service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    //搜索
    @GetMapping("/list")
    public PageResult search(@RequestBody RequestParams params){
        return articleService.search(params);
    }

    //搜索框自动补全
    @GetMapping("suggestion")
    public List<String> getSuggestions(@RequestParam("key") String prefix){
        return articleService.getSuggestions(prefix);
    }
}
