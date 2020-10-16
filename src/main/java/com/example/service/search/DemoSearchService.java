package com.example.service.search;

import com.example.dto.DemoDto;
import com.example.dto.DemoSearchDto;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:47
 */
public interface DemoSearchService {

    List<DemoDto> search(DemoSearchDto demoSearchDto);

}
