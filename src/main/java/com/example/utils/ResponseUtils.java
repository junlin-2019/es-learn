package com.example.utils;

import com.example.dto.ResponseDto;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 14:42
 */
public class ResponseUtils {
    private static Integer SUCCESS = 200;
    private static Integer FAIL = 500;

    public static ResponseDto success (){
        ResponseDto responseDto = new ResponseDto();
        responseDto.setCode(SUCCESS);
        return responseDto;
    }
    public static ResponseDto success (Object data){
        ResponseDto responseDto = new ResponseDto();
        responseDto.setCode(SUCCESS);
        responseDto.setData(data);
        return responseDto;
    }

    public static ResponseDto error (String message){
        ResponseDto responseDto = new ResponseDto();
        responseDto.setCode(500);
        responseDto.setMessage(message);
        return responseDto;
    }
}
