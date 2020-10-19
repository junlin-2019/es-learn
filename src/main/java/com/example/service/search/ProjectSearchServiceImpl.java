package com.example.service.search;

import com.example.constants.ProjectConstant;
import com.example.dto.CityMaxDto;
import com.example.dto.ProjectDto;
import com.example.dto.ProjectResponseDto;
import com.example.dto.ProjectSearchDto;
import com.example.service.docs.ProjectDoc;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:49
 */
@Service
@Slf4j
public class ProjectSearchServiceImpl  implements ProjectSearchService{

    @Autowired
    private RestHighLevelClient client;


    @Override
    public ProjectResponseDto search(ProjectSearchDto bo) {
        SearchRequest searchRequest = new SearchRequest(ProjectConstant.ALIAS);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤条件
        addCbsProjectInfoFilterQuery(boolQueryBuilder, bo);
        sourceBuilder.query(boolQueryBuilder);
        // 关键字
        addKeywordQuery(boolQueryBuilder, bo.getKeyWord());
        sourceBuilder.query(boolQueryBuilder);

        //计算距离
        boolean distanceFlag = false;
        if (StringUtils.isNoneBlank(bo.getLat(), bo.getLng())) {
            GeoDistanceSortBuilder geoDistanceSortBuilder = SortBuilders.geoDistanceSort("location", new GeoPoint(Double.valueOf(bo.getLat()),Double.valueOf(bo.getLng()) ));
            geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS);
            geoDistanceSortBuilder.order(SortOrder.ASC);
            geoDistanceSortBuilder.geoDistance(GeoDistance.ARC);
            sourceBuilder.sort(geoDistanceSortBuilder);
            distanceFlag = true;
        }

        // 分页
        addPagination(sourceBuilder, bo);

        // 排序
        addSort(sourceBuilder, bo);

        // 聚合
        addProjectInfoAggregation(sourceBuilder, bo);

        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            if (response.status().equals(RestStatus.OK)) {
                ProjectResponseDto responseDto = new ProjectResponseDto();
                getHits(response, responseDto, distanceFlag);
                getAggregation(response, responseDto, bo);
                return responseDto;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private void addCbsProjectInfoFilterQuery(BoolQueryBuilder boolQueryBuilder, ProjectSearchDto bo) {
        // cityId
        if (StringUtils.isNotBlank(bo.getCityId())) {
            TermQueryBuilder cityQueryBuilder = QueryBuilders.termQuery("cityId", bo.getCityId());
            boolQueryBuilder.filter(cityQueryBuilder);
        }
        // 项目类型多选
        if (!CollectionUtils.isEmpty(bo.getProjectTypeList())) {
            TermsQueryBuilder projectTypeIdQueryBuilder = QueryBuilders.termsQuery("typeList.id",
                    bo.getProjectTypeList());
            boolQueryBuilder.filter(projectTypeIdQueryBuilder);
        }
        // 项目状态
        if (StringUtils.isNotBlank(bo.getOpenStatus())) {
            TermQueryBuilder projectStatusQueryBuilder = QueryBuilders.termQuery("openStatus", bo.getOpenStatus());
            boolQueryBuilder.filter(projectStatusQueryBuilder);
        }

        // 商业面积
        if (StringUtils.isNotBlank(bo.getArea())) {
            String gteCommercialArea = StringUtils.substringBefore(bo.getArea(), ",");
            String lteCommercialArea = StringUtils.substringAfter(bo.getArea(), ",");
            RangeQueryBuilder commercialAreaRange = QueryBuilders.rangeQuery("area");
            if (StringUtils.isNotBlank(gteCommercialArea)) {
                commercialAreaRange.gte(Double.valueOf(gteCommercialArea));
            }
            if (StringUtils.isNotBlank(lteCommercialArea)) {
                commercialAreaRange.lte(Double.valueOf(lteCommercialArea));
            }
            boolQueryBuilder.filter(commercialAreaRange);
        }

        // 半径
        if (StringUtils.isNoneBlank(bo.getLat(), bo.getLng(), bo.getDistance())) {
            GeoDistanceQueryBuilder locationDistanceQueryBuilder = QueryBuilders.geoDistanceQuery("location")
                    .point(new GeoPoint(bo.getLat() + "," + bo.getLng()))
                    .distance(bo.getDistance(), DistanceUnit.METERS);
            boolQueryBuilder.filter(locationDistanceQueryBuilder);
        }

        // 多边形
        if (StringUtils.isNotBlank(bo.getFScope())) {

            List<GeoPoint> points = Arrays.asList(StringUtils.split(bo.getFScope(), "|")).stream().map(
                    p -> new GeoPoint(StringUtils.substringAfter(p, ",") + "," + StringUtils.substringBefore(p, ",")))
                    .collect(Collectors.toList());

            GeoPolygonQueryBuilder locationPolygonQueryBuilder = QueryBuilders.geoPolygonQuery("location", points);
            boolQueryBuilder.filter(locationPolygonQueryBuilder);
        }

        // 区县id
        if (!CollectionUtils.isEmpty(bo.getDistrictIdList())) {
            TermsQueryBuilder districtIdQueryBuilder = QueryBuilders.termsQuery("industryDistrictId",
                    bo.getDistrictIdList());
            boolQueryBuilder.filter(districtIdQueryBuilder);
        }

    }

    private void addKeywordQuery(BoolQueryBuilder boolQueryBuilder, String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            BoolQueryBuilder keywordQueryBuilder = QueryBuilders.boolQuery();
            // 项目名
            keywordQueryBuilder.should(
                    new MatchQueryBuilder("name", keyword).analyzer("ik_max_word"));
            // 项目描述
            keywordQueryBuilder.should(new MatchQueryBuilder("desc", keyword)
                    .analyzer("ik_max_word"));

            boolQueryBuilder.must(keywordQueryBuilder);
        }
    }

    private void addPagination(SearchSourceBuilder sourceBuilder, ProjectSearchDto bo) {
        if ((bo.getAggByDistrict() != null && bo.getAggByDistrict())) {
            // 聚合不分页
            sourceBuilder.size(0);
        } else {
            int page = 1;
            int pageSize = 20;
            if (bo.getPageNum() != null) {
                page = bo.getPageNum();
            }
            if (bo.getPageSize() != null && bo.getPageSize() < 250) {
                pageSize = bo.getPageSize();
            }
            int count = page * pageSize;
            if(count>10000){
                page = 500;
                pageSize = 20;
            }
            // 分页不具合
            sourceBuilder.from((page-1) * pageSize).size(pageSize);
        }
    }

    private void addSort(SearchSourceBuilder sourceBuilder, ProjectSearchDto bo) {
        sourceBuilder.sort("area", SortOrder.DESC);
    }

    private void addProjectInfoAggregation(SearchSourceBuilder sourceBuilder, ProjectSearchDto bo) {
        if (bo.getAggByDistrict() != null && bo.getAggByDistrict()) {
            sourceBuilder.aggregation(AggregationBuilders.terms("byCityId").field("cityId")
                    .size(500).subAggregation(
                            AggregationBuilders.max("maxPrice").field("price")));
        }
    }

    private void getHits(SearchResponse response, ProjectResponseDto responseDto ,boolean dintanceFlag) {
        List<ProjectDto> projectDtos = new ArrayList<>();
        responseDto.setNumber(Long.valueOf(response.getHits().totalHits).intValue());
        for (SearchHit hit : response.getHits().getHits()) {
            try {
                ProjectDoc doc = new ObjectMapper().readValue(hit.getSourceAsString(), ProjectDoc.class);
                ProjectDto projectDto = new ProjectDto();
                BeanUtils.copyProperties(doc,projectDto);
                if(projectDto.getArea()!=null){
                    projectDto.setArea(projectDto.getArea().setScale(2, RoundingMode.HALF_UP));
                }
                if(projectDto.getPrice()!= null){
                    projectDto.setPrice(projectDto.getPrice().setScale(2, RoundingMode.HALF_UP));
                }
                if(dintanceFlag){
                    BigDecimal geoDis=new BigDecimal((double)hit.getSortValues()[0]);
                    projectDto.setDistince(geoDis.setScale(1,RoundingMode.HALF_UP).doubleValue());
                }
                projectDtos.add(projectDto);
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        responseDto.setProjectDtos(projectDtos);
    }


    private void getAggregation(SearchResponse response, ProjectResponseDto responseDto,
                                ProjectSearchDto bo) {

        if (bo.getAggByDistrict() != null && bo.getAggByDistrict()) {
            List<CityMaxDto> cityMaxDtos = new ArrayList<>();
            Aggregations agg = response.getAggregations();
            Terms terms = agg.get("byCityId");
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                String cityId = bucket.getKeyAsString();
                Max maxPrice = bucket.getAggregations().get("maxPrice");
                CityMaxDto cityMaxDto = new CityMaxDto();
                cityMaxDto.setCityId(cityId);
                cityMaxDto.setPrice(maxPrice.getValueAsString());
                cityMaxDtos.add(cityMaxDto);
            }
            responseDto.setCityMaxDtos(cityMaxDtos);
        }
    }

}
