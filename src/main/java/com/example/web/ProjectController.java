package com.example.web;

import com.example.dto.BlogDto;
import com.example.dto.BlogSearchDto;
import com.example.dto.IndexDto;
import com.example.dto.ProjectResponseDto;
import com.example.dto.ProjectSearchDto;
import com.example.dto.ResponseDto;
import com.example.service.document.BlogDocumentService;
import com.example.service.document.ProjectDocumentService;
import com.example.service.index.BlogIndexService;
import com.example.service.search.ProjectSearchService;
import com.example.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 14:40
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectDocumentService projectDocumentService;

    @Autowired
    private ProjectSearchService projectSearchService;


    @PostMapping("/buildIndex")
    public ResponseDto buildIndex(){
        projectDocumentService.bulidIndexAndInit();
        return ResponseUtils.success();
    }


    @PostMapping("/search")
    public ResponseDto search(@RequestBody ProjectSearchDto projectSearchDto){
        ProjectResponseDto search = projectSearchService.search(projectSearchDto);
        return ResponseUtils.success(search);
    }

}
