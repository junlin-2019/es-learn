package com.example.service.search;

import com.example.constants.BlogConstant;
import com.example.dto.BlogDto;
import com.example.dto.BlogSearchDto;
import com.example.service.docs.BlogDoc;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/19 10:27
 */
@Service
@Slf4j
public class BlogSearchServiceImpl  implements BlogSearchService{

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<BlogDto> search(BlogSearchDto blogSearchDto) {
        SearchRequest searchRequest = new SearchRequest(BlogConstant.ALIAS);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        addKeywordQuery(boolQueryBuilder,blogSearchDto.getKeyWord());
        sourceBuilder.query(boolQueryBuilder);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            if (response.status().equals(RestStatus.OK)) {
                List<BlogDto> list = new ArrayList<>();
                getHits(response, list);
                return list;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private void addKeywordQuery(BoolQueryBuilder boolQueryBuilder, String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            BoolQueryBuilder keywordQueryBuilder = QueryBuilders.boolQuery();
            // 博客名称
            keywordQueryBuilder.should(
                    new MatchQueryBuilder("blogName", keyword).boost(3).analyzer("ik_max_word").operator(Operator.AND));
            // 作者
            keywordQueryBuilder.should(new MatchQueryBuilder("author", keyword).boost(2)
                    .analyzer("ik_max_word").operator(Operator.AND));
            // 内容
            keywordQueryBuilder.should(new MatchQueryBuilder("content", keyword).boost(1).analyzer("ik_max_word")
                    .operator(Operator.AND));
            boolQueryBuilder.must(keywordQueryBuilder);
        }
    }

    private void getHits(SearchResponse response,List<BlogDto> list) throws IOException {
        for (SearchHit hit : response.getHits().getHits()) {
            BlogDoc blogDoc = new ObjectMapper().readValue(hit.getSourceAsString(), BlogDoc.class);
            BlogDto blogDto = new BlogDto();
            BeanUtils.copyProperties(blogDoc,blogDto);
            list.add(blogDto);
        }
    }
}
