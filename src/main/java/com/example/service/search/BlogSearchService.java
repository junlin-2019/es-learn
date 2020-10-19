package com.example.service.search;

import com.example.dto.BlogDto;
import com.example.dto.BlogSearchDto;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:46
 */
public interface BlogSearchService {
    List<BlogDto> search(BlogSearchDto blogSearchDto);
}
