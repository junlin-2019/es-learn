package com.example.service.search;

import com.example.dto.ProjectResponseDto;
import com.example.dto.ProjectSearchDto;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:47
 */
public interface ProjectSearchService {

    ProjectResponseDto search(ProjectSearchDto projectSearchDto);

}
