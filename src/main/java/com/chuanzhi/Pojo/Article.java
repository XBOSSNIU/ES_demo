package com.chuanzhi.Pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("article")
@NoArgsConstructor
public class Article {
    @TableId
    private Long id;
    private String writer;
    private String name;
    private String type;
    private String info;
    private Long price;
    private String publisher;
}
