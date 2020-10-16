package com.example.web;

import com.example.dto.BlogDto;
import com.example.dto.IndexDto;
import com.example.dto.ResponseDto;
import com.example.service.document.BlogDocumentService;
import com.example.service.index.BlogIndexService;
import com.example.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 14:40
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogDocumentService blogDocumentService;

    @Autowired
    private BlogIndexService blogIndexService;

    @PostMapping("/buildIndex")
    public ResponseDto buildIndex(){
        blogDocumentService.bulidIndexAndInit();
        return ResponseUtils.success();
    }

    @PostMapping("/test")
    public ResponseDto testException(){
        int a = 1/0;
        return ResponseUtils.success();
    }

    @PostMapping("/index")
    public ResponseDto index(@RequestBody IndexDto<BlogDto> indexDto){
        boolean index = blogIndexService.index(indexDto.getIndex(), indexDto.getData());
        if(index){
            return ResponseUtils.success();
        }else{
            return ResponseUtils.error("index异常");
        }

    }


}
