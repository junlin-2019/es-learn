package com.example.exception;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 13:48
 */
public class IndexDataRuntimeException extends RuntimeException{

    public IndexDataRuntimeException(String message){
        super(message);
    }
    public IndexDataRuntimeException(){
    }
}
