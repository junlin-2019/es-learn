package com.example.dto;

import lombok.Data;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 13:25
 */
@Data
public class BlogDto {

    private Long blogId;

    private String blogName;

    private String author;

    private String content;
}
