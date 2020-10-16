package com.example.service.index;

import com.example.dto.BlogDto;
import com.example.enums.SyncDataStatusEnum;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 13:24
 */
public interface BlogIndexService {

        boolean index(String indexName,BlogDto blogDto);

        SyncDataStatusEnum syncFromDataBase(String indexName);
}
