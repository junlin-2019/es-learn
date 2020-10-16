package com.example.service.docs;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 18:08
 */
@Data
public class DemoDoc {
    private Integer id;
    private String name;
    private String nameShortLetter;
    private String imgUrl;
    private String newcode;
    private String openStatus;
    private Integer openStatusOrder;
    private String openDate;
    private String openDateYear;
    private String address;
    private String cityName;
    private String cityId;
    private String districtName;
    private String industryDistrictId;
    private String comareaName;
    private String comareaId;
    private BigDecimal commercialArea;
    private Integer commercialFloorMin;
    private Integer commercialFloorMax;
    private String developerName;
    private String developerEnterpriseId;
    private String developerTycId;
    private String investmentStatus;
    private Integer investmentStatusOrder;
    private String isChainProject;
    private Integer isChainProjectOrder;
    private Integer rentDate;
    private BigDecimal rentUnitPrice;
    private Integer saleDate;
    private BigDecimal saleAmount;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer hasShop;
    private String location;
    private String projectEstateTypeKeyword;
    private Set<Object> investmentCategoryList;
    private Set<Object> projectEstateTypeList;
    private String projectEstateTypeOrder;
    private String hasImg;
}
