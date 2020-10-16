package com.example.service.search;

import com.example.dto.DemoDetailDto;
import com.example.dto.DemoDto;
import com.example.dto.DemoSearchDto;
import com.example.dto.DemoComareaCountDto;
import com.example.service.docs.DemoDoc;
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
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
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
public class DemoSearchServiceImpl  implements DemoSearchService{

    @Autowired
    private RestHighLevelClient client;

    private static String DEMO_ALIAS = "demo.alias";
    @Override
    public List<DemoDto> search(DemoSearchDto bo) {
        SearchRequest searchRequest = new SearchRequest(DEMO_ALIAS);
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
        if (StringUtils.isNoneBlank(bo.getCenterX(), bo.getCenterY())) {
            GeoDistanceSortBuilder geoDistanceSortBuilder = SortBuilders.geoDistanceSort("location", new GeoPoint(Double.valueOf(bo.getCenterY()),Double.valueOf(bo.getCenterX()) ));
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
        addCbsProjectInfoAggregation(sourceBuilder, bo);

        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            if (response.status().equals(RestStatus.OK)) {
                DemoDto demoDto = new DemoDto();
                getHits(response, demoDto,distanceFlag);
                getAggregation(response, demoDto, bo);

      }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private void addCbsProjectInfoFilterQuery(BoolQueryBuilder boolQueryBuilder, DemoSearchDto bo) {
        // cityId
        if (StringUtils.isNotBlank(bo.getCityId())) {
            TermQueryBuilder cityQueryBuilder = QueryBuilders.termQuery("cityId", bo.getCityId());
            boolQueryBuilder.filter(cityQueryBuilder);
        }
        // 项目类型多选
        if (!CollectionUtils.isEmpty(bo.getProjectEstateTypeIdList())) {
            TermsQueryBuilder projectTypeIdQueryBuilder = QueryBuilders.termsQuery("projectEstateTypeList.id",
                    bo.getProjectEstateTypeIdList());
            boolQueryBuilder.filter(projectTypeIdQueryBuilder);
        }
        // 项目状态
        if (StringUtils.isNotBlank(bo.getOpenStatus())) {
            TermQueryBuilder projectStatusQueryBuilder = QueryBuilders.termQuery("openStatus", bo.getOpenStatus());
            boolQueryBuilder.filter(projectStatusQueryBuilder);
        }

        // 是否连锁
        if (bo.getHasChainProject() != null) {
            TermQueryBuilder projectStatusQueryBuilder = QueryBuilders.termQuery("isChainProject",
                    bo.getHasChainProject() == true ? "是" : "否");
            boolQueryBuilder.filter(projectStatusQueryBuilder);
        }
        // 商业面积
        if (StringUtils.isNotBlank(bo.getCommercialArea())) {
            String gteCommercialArea = StringUtils.substringBefore(bo.getCommercialArea(), ",");
            String lteCommercialArea = StringUtils.substringAfter(bo.getCommercialArea(), ",");
            RangeQueryBuilder commercialAreaRange = QueryBuilders.rangeQuery("commercialArea");
            if (StringUtils.isNotBlank(gteCommercialArea)) {
                commercialAreaRange.gte(Double.valueOf(gteCommercialArea));
            }
            if (StringUtils.isNotBlank(lteCommercialArea)) {
                commercialAreaRange.lte(Double.valueOf(lteCommercialArea));
            }
            boolQueryBuilder.filter(commercialAreaRange);
        }

        // 开业时间
        if (StringUtils.isNotBlank(bo.getOpenDate())) {
            String gteOpenDate = StringUtils.substringBefore(bo.getOpenDate(), ",");
            String lteOpenDate = StringUtils.substringAfter(bo.getOpenDate(), ",");
            RangeQueryBuilder openDateRange = QueryBuilders.rangeQuery("openDateYear");
            if (StringUtils.isNotBlank(gteOpenDate)) {
                openDateRange.gte(gteOpenDate);
            }
            if (StringUtils.isNotBlank(lteOpenDate)) {
                openDateRange.lte(lteOpenDate);
            }
            boolQueryBuilder.filter(openDateRange);
        }

        // 租金单价
        if (StringUtils.isNotBlank(bo.getRentUnitPrice())) {
            String gteRentUnitPrice = StringUtils.substringBefore(bo.getRentUnitPrice(), ",");
            String lteRentUnitPrice = StringUtils.substringAfter(bo.getRentUnitPrice(), ",");
            RangeQueryBuilder rentUnitPriceRange = QueryBuilders.rangeQuery("rentUnitPrice");
            if (StringUtils.isNotBlank(gteRentUnitPrice)) {
                rentUnitPriceRange.gte(Double.valueOf(gteRentUnitPrice));
            }
            if (StringUtils.isNotBlank(lteRentUnitPrice)) {
                rentUnitPriceRange.lte(Double.valueOf(lteRentUnitPrice));
            }
            boolQueryBuilder.filter(rentUnitPriceRange);
        }

        // 招商状态
        if (StringUtils.isNotBlank(bo.getInvestmentStatus())) {
            TermQueryBuilder investmentStatusBuilder = QueryBuilders.termQuery("investmentStatus",
                    bo.getInvestmentStatus());
            boolQueryBuilder.filter(investmentStatusBuilder);
        }

        // 招商需求
        if (!CollectionUtils.isEmpty(bo.getInvestmentCategoryIdList())) {
            TermsQueryBuilder investmentCategoryIdQueryBuilder = QueryBuilders.termsQuery("investmentCategoryList.id",
                    bo.getInvestmentCategoryIdList());
            boolQueryBuilder.filter(investmentCategoryIdQueryBuilder);
        }

        // 左上右下坐标
        if (StringUtils.isNoneBlank(bo.getMaxLat(), bo.getMaxLng(), bo.getMinLat(), bo.getMinLng())) {
            GeoBoundingBoxQueryBuilder locationBoxQueryBuilder = QueryBuilders.geoBoundingBoxQuery("location")
                    .setCornersOGC(new GeoPoint(bo.getMinLat() + "," + bo.getMinLng()),
                            new GeoPoint(bo.getMaxLat() + "," + bo.getMaxLng()));
            boolQueryBuilder.filter(locationBoxQueryBuilder);
        }

        // 半径
        if (StringUtils.isNoneBlank(bo.getCenterX(), bo.getCenterY(), bo.getTargetDistance())) {
            GeoDistanceQueryBuilder locationDistanceQueryBuilder = QueryBuilders.geoDistanceQuery("location")
                    .point(new GeoPoint(bo.getCenterY() + "," + bo.getCenterX()))
                    .distance(bo.getTargetDistance(), DistanceUnit.METERS);
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
        if (!CollectionUtils.isEmpty(bo.getIndustryDistrictIdList())) {
            TermsQueryBuilder districtIdQueryBuilder = QueryBuilders.termsQuery("industryDistrictId",
                    bo.getIndustryDistrictIdList());
            boolQueryBuilder.filter(districtIdQueryBuilder);
        }

        // 商圈id
        if (!CollectionUtils.isEmpty(bo.getComareaIdList())) {
            TermsQueryBuilder comareaIdQueryBuilder = QueryBuilders.termsQuery("comareaId", bo.getComareaIdList());
            boolQueryBuilder.filter(comareaIdQueryBuilder);
        }

    }

    private void addKeywordQuery(BoolQueryBuilder boolQueryBuilder, String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            BoolQueryBuilder keywordQueryBuilder = QueryBuilders.boolQuery();
            // 项目名
            keywordQueryBuilder.should(
                    new MatchQueryBuilder("name", keyword).boost(4).analyzer("ik_max_word").operator(Operator.AND));
            // 项目类型
            keywordQueryBuilder.should(new MatchQueryBuilder("projectEstateTypeKeyword", keyword).boost(2)
                    .analyzer("ik_max_word").operator(Operator.AND));
            // 开发商
            keywordQueryBuilder.should(new MatchQueryBuilder("developerName", keyword).boost(1).analyzer("ik_max_word")
                    .operator(Operator.AND));
            boolQueryBuilder.must(keywordQueryBuilder);
        }
    }

    private void addPagination(SearchSourceBuilder sourceBuilder, DemoSearchDto bo) {
        if ((bo.getAggByComarea() != null && bo.getAggByComarea())
                || (bo.getAggByDistrict() != null && bo.getAggByDistrict())) {
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

    private void addSort(SearchSourceBuilder sourceBuilder, DemoSearchDto bo) {
        if (StringUtils.isNotBlank(bo.getOrderIndex()) && null != bo.getOrderType()) {
            sourceBuilder.sort(bo.getOrderIndex(), SortOrder.fromString(bo.getOrderType().toString()));
        } else {
            sourceBuilder.sort("hasImg", SortOrder.DESC);
            sourceBuilder.sort("hasShop", SortOrder.DESC);
            sourceBuilder.sort("projectEstateTypeOrder", SortOrder.ASC);
            sourceBuilder.sort("commercialArea", SortOrder.DESC);
            sourceBuilder.sort("openStatusOrder", SortOrder.DESC);
            sourceBuilder.sort("isChainProjectOrder", SortOrder.DESC);
            sourceBuilder.sort("nameShortLetter", SortOrder.ASC);
        }

    }

    private void addCbsProjectInfoAggregation(SearchSourceBuilder sourceBuilder, DemoSearchDto bo) {
        if (bo.getAggByComarea() != null && bo.getAggByComarea()) {
            sourceBuilder
                    .aggregation(AggregationBuilders.terms("comareaId").size(500).field("comareaId").subAggregation(
                            AggregationBuilders.topHits("comareaName").size(1).fetchSource("comareaName", null)));
        }
        if (bo.getAggByDistrict() != null && bo.getAggByDistrict()) {
            sourceBuilder.aggregation(AggregationBuilders.terms("industryDistrictId").field("industryDistrictId")
                    .size(500).subAggregation(
                            AggregationBuilders.topHits("districtName").size(1).fetchSource("districtName", null)));
        }
    }

    private void getHits(SearchResponse response, DemoDto demoDto ,boolean dintanceFlag) {
        List<DemoDetailDto> projectDetailDataDtoList = new ArrayList<>();
        demoDto.setNumber(Long.valueOf(response.getHits().totalHits).intValue());
        Arrays.stream(response.getHits().getHits()).forEach(i -> {
            try {
                DemoDoc d = new ObjectMapper().readValue(i.getSourceAsString(), DemoDoc.class);
                DemoDetailDto p = new DemoDetailDto();
                p.setComarea(d.getComareaName());
                if (d.getCommercialArea() != null) {
                    p.setCommercialArea(d.getCommercialArea().setScale(2, RoundingMode.HALF_UP).toString());
                }
                p.setCommercialFloorMax(d.getCommercialFloorMax());
                p.setCommercialFloorMin(d.getCommercialFloorMin());
                p.setDistrict(d.getDistrictName());
                p.setGdLat(d.getLat().toString());
                p.setGdLng(d.getLng().toString());
                p.setImgUrl(d.getImgUrl());
                p.setInvestmentStatus(d.getInvestmentStatus());
                p.setNewcode(d.getNewcode());
                p.setOpenDate(d.getOpenDate());
                p.setOpenStatus(d.getOpenStatus());
                p.setProjectId(d.getId());
                p.setProjectName(d.getName());
                if(dintanceFlag){
                    BigDecimal geoDis=new BigDecimal((double)i.getSortValues()[0]);
                    p.setDistance(geoDis.setScale(1,RoundingMode.HALF_UP).toString());
                }
                projectDetailDataDtoList.add(p);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        demoDto.setDemoDetailDtos(projectDetailDataDtoList);
    }


    private void getAggregation(SearchResponse response, DemoDto demoDto,
                                DemoSearchDto bo) {

        if (bo.getAggByComarea() != null && bo.getAggByComarea()) {
            // 获取商圈最新一条租金数据
            List<DemoComareaCountDto> demoComareaCountDtoList = new ArrayList<>();
            Aggregations g = response.getAggregations();
            Terms comareaIdTerm = g.get("comareaId");
            for (Terms.Bucket b : comareaIdTerm.getBuckets()) {
                DemoComareaCountDto c = new DemoComareaCountDto();
                c.setComareaId(b.getKeyAsNumber().intValue());
                c.setProjectNum(Long.valueOf(b.getDocCount()).intValue());
                TopHits h = b.getAggregations().get("comareaName");
                c.setComarea(h.getHits().getHits()[0].getSourceAsMap().get("comareaName").toString());
                demoComareaCountDtoList.add(c);
            }
            demoDto.setDemoComareaCountDtos(demoComareaCountDtoList);
        }
    }

}
