package com.example.dto;

import lombok.Data;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 14:41
 */
@Data
public class ResponseDto {

    private Integer code;

    private String message;

    private Object data;
}
