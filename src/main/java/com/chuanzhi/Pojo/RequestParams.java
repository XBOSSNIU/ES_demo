package com.chuanzhi.Pojo;

import lombok.Data;

@Data
public class RequestParams {
    String key;
    private Integer page;
    private Integer size;
    private String sortBy;
}
