package com.example.service.es;

import com.example.enums.SyncDataStatusEnum;
import com.example.exception.IndexDataRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 10:57
 */
@Service
@Slf4j
public class EsServiceImpl implements IEsService {

    private static final String ES_DEFAULT_TYPE = "_doc";
    private static final int INSERTDOC_BATCHSIZE = 20;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public boolean createIndex(String index, String alias, String mappingSource) {
        // 1、创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 1));
        if (!StringUtils.isEmpty(mappingSource)) {
            request.mapping(ES_DEFAULT_TYPE, mappingSource, XContentType.JSON);
        }
        if (!StringUtils.isEmpty(alias)) {
            request.alias(new Alias(alias));
        }
        // 2、客户端执行请求 IndicesClient,请求后获得响应
        try {
            if (!existsIndex(index)) {
                CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
                return createIndexResponse.isAcknowledged();
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteIndex(String index) {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(index);
        try {
            if (client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
                DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
                return client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT).isAcknowledged();
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existsIndex(String index) {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public List<String> getAlias(String alias) {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(alias);
        try {
            GetAliasesResponse response = client.indices().getAlias(request, RequestOptions.DEFAULT);
            return response.getAliases().keySet().stream().collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;

    }

    @Override
    public boolean addAlias(String index, String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest();
        AliasActions add = new AliasActions(AliasActions.Type.ADD);
        add.alias(alias);
        add.index(index);
        request.addAliasAction(add);
        try {

            return client.indices().updateAliases(request, RequestOptions.DEFAULT).isAcknowledged();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean switchAlias(String oldIndex, String newIndex, String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest();
        AliasActions add = new AliasActions(AliasActions.Type.ADD);
        add.alias(alias);
        add.index(newIndex);

        AliasActions remove = new AliasActions(AliasActions.Type.REMOVE);
        remove.alias(alias);
        remove.index(oldIndex);

        request.addAliasAction(add).addAliasAction(remove);
        try {

            return client.indices().updateAliases(request, RequestOptions.DEFAULT).isAcknowledged();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public SyncDataStatusEnum insertByTask(String index, List<? extends EsDoc> docs) {
        log.info("insertByTask:start");
        List<String> list = Collections.synchronizedList(new ArrayList<>());
        boolean complete = new ForkJoinPool().invoke(new InsertDocTask(index, docs, docs.size(), list));
        if (complete) {
            if (list.size() > 0) {
                for (String s : list) {
                    log.error(s);
                }
                return SyncDataStatusEnum.part_success;
            } else {
                return SyncDataStatusEnum.success;
            }
        }
        return SyncDataStatusEnum.fail;
    }

    @Override
    public SyncDataStatusEnum insertSingle(String index, EsDoc docs) {
        try {
            IndexRequest request = new IndexRequest(index);
            request.timeout(new TimeValue(60000));
            request.id(docs.docId());
            request.type(ES_DEFAULT_TYPE);
            String jsonString = null;
            jsonString = new ObjectMapper().writeValueAsString(docs);
            request.source(jsonString, XContentType.JSON);
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            log.info("create result:" + indexResponse.status().getStatus());
            if (indexResponse.status().equals(RestStatus.CREATED)) {
                return SyncDataStatusEnum.success;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SyncDataStatusEnum.fail;
    }

    @Override
    public SyncDataStatusEnum batchInsert(String index, List<EsDoc> docs) {
        BulkRequest batch = new BulkRequest();
        try {
            for (EsDoc d : docs) {
                IndexRequest request = new IndexRequest(index);
                request.timeout(new TimeValue(60000));
                request.id(d.docId());
                request.type(ES_DEFAULT_TYPE);
                String jsonString = null;

                jsonString = new ObjectMapper().writeValueAsString(d);

                request.source(jsonString, XContentType.JSON);
                batch.add(request);
            }
            BulkResponse bulkResponse = client.bulk(batch, RequestOptions.DEFAULT);
            if(bulkResponse.hasFailures()){
                List<String> errorList = new ArrayList<>();
                for (BulkItemResponse i : bulkResponse.getItems()) {
                    if (i.getFailure() != null) {
                        errorList.add(i.getFailure().getId() + ":" + i.getFailure().getMessage());
                    }
                }
                if(!CollectionUtils.isEmpty(errorList)){
                    throw new IndexDataRuntimeException("未索引成功的数据" + new ObjectMapper().writeValueAsString(errorList));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SyncDataStatusEnum.success;
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) {
        try {
            return client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private class InsertDocTask extends RecursiveTask<Boolean> {

        private static final long serialVersionUID = -7868500150024264981L;
        private List<? extends EsDoc> docs;
        private int size;
        private String index;
        private List<String> errorList;

        public InsertDocTask(String index, List<? extends EsDoc> docs, int size, List<String> errorList) {
            this.index = index;
            this.docs = docs;
            this.size = size;
            this.errorList = errorList;
        }

        @Override
        protected Boolean compute() {
            if ((size) > INSERTDOC_BATCHSIZE) {
                int middle = size / 2;
                List<? extends EsDoc> docs1 = docs.subList(0, middle);
                InsertDocTask t1 = new InsertDocTask(index, docs1, docs1.size(), errorList);
                List<? extends EsDoc> docs2 = docs.subList(middle, size);
                InsertDocTask t2 = new InsertDocTask(index, docs2, docs2.size(), errorList);
                try {
                    invokeAll(t1, t2);
                    return t1.get() && t2.get();
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                } catch (ExecutionException e) {
                    log.error(e.getMessage());
                }
                return false;
            } else {
                return createJobBulk(index, docs, errorList);
            }
        }
    }

    private boolean createJobBulk(String index, List<? extends EsDoc> docs, List<String> errorList) {
        try {
            BulkRequest batch = new BulkRequest();
            BulkResponse bulkResponse;
            for (EsDoc d : docs) {
                IndexRequest request = new IndexRequest(index);
                request.timeout(new TimeValue(60000));
                request.id(d.docId());
                request.type(ES_DEFAULT_TYPE);
                String jsonString = new ObjectMapper().writeValueAsString(d);
                request.source(jsonString, XContentType.JSON);
                batch.add(request);
            }
            bulkResponse = client.bulk(batch, RequestOptions.DEFAULT);
            log.info("created:" + docs.size() + "," + bulkResponse.status() + "," + bulkResponse.getItems().length);
            for (BulkItemResponse i : bulkResponse.getItems()) {
                if (i.getFailure() != null) {
                    errorList.add(i.getFailure().getId() + ":" + i.getFailure().getMessage());
                }
            }
            return bulkResponse.status().equals(RestStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }
}

