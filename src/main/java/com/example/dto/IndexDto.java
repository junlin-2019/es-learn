package com.example.dto;

import lombok.Data;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:16
 */
@Data
public class IndexDto<E> {
    private String index;
    private E data;
}
