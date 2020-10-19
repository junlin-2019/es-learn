package com.example.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:48
 */
@Data
public class ProjectSearchDto {

    private String cityId;

    private String keyWord;

    private List<Integer> projectTypeList;

    private String openStatus;

    private String area;

    private String price;

    private Integer pageNum;

    private Integer pageSize;

    private String lat;

    private String lng;

    private String distance;

   //"116.195864,39.928876|116.230359,39.928876|116.230359,39.956756|116.195864,39.956756"
    private String fScope;

    private List<Integer> districtIdList;

    private Boolean aggByDistrict;
}
