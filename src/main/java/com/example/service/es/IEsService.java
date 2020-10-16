package com.example.service.es;

import com.example.enums.SyncDataStatusEnum;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 10:54
 */
public interface IEsService {
    boolean createIndex(String index, String alias, String mappingSource);

    boolean deleteIndex(String index);

    boolean existsIndex(String index);

    List<String> getAlias(String alias);

    boolean addAlias(String index, String alias);

    boolean switchAlias(String oldIndex, String newIndex, String alias);

    SyncDataStatusEnum insertByTask(String index, List<? extends EsDoc> docs);

    SyncDataStatusEnum insertSingle(String index, EsDoc docs);

    SyncDataStatusEnum batchInsert(String index, List<EsDoc> docs);

    SearchResponse search(SearchRequest searchRequest);
}

