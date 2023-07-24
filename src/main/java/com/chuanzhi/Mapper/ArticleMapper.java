package com.chuanzhi.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chuanzhi.Pojo.Article;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
