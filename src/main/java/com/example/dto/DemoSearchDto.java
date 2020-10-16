package com.example.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:48
 */
@Data
public class DemoSearchDto {

    private String cityId;

    private String cityName;


    private String keyWord;

    private List<Integer> projectEstateTypeIdList;


    private String openStatus;

    private Boolean hasChainProject;

    private String commercialArea;

    private String openDate;

    private String rentUnitPrice;

    private String investmentStatus;

    private List<Integer> investmentCategoryIdList;

    private Integer pageNum;

    private Integer pageSize;

    private String orderIndex;

    private String orderType;

    private String minLat;

    private String minLng;

    private String maxLat;

    private String maxLng;

    private String centerY;

    private String centerX;

    private String targetDistance;

   //"116.195864,39.928876|116.230359,39.928876|116.230359,39.956756|116.195864,39.956756"
    private String fScope;

    private List<Integer> industryDistrictIdList;

    private List<Integer> comareaIdList;

    private Boolean aggByDistrict;

    private Boolean aggByComarea;
}
