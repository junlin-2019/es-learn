package com.example.config;

import com.example.dto.ResponseDto;
import com.example.exception.IndexDataRuntimeException;
import com.example.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 14:48
 */
@ControllerAdvice
@Slf4j
public class RunTimeExceptionHandler {

    @ExceptionHandler(IndexDataRuntimeException.class)
    @ResponseBody
    public ResponseDto handlerSyncData(Exception exception){
        return ResponseUtils.error(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseDto handlerException(Exception exception){
        exception.printStackTrace();
        log.error(exception.getMessage());
        return ResponseUtils.error(exception.getMessage());
    }
}
