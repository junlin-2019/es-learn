package com.example.service.index;

import com.example.dto.BlogDto;
import com.example.dto.ProjectDto;
import com.example.enums.SyncDataStatusEnum;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 13:24
 */
public interface ProjectIndexService {

        boolean index(String indexName, ProjectDto projectDto);

        SyncDataStatusEnum syncFromDataBase(String indexName);
}
