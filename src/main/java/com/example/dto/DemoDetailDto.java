package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 18:11
 */
@Data
public class DemoDetailDto {


    private String newcode;


    private Integer projectId;


    private String projectName;


    private String address;

    private String district;


    private String comarea;


    private String projectEstateType;


    private String openStatus;


    private Boolean hasChainProject;


    private String investmentStatus;


    private String commercialArea;


    private String openDate;


    private String developer;

    private String rentUnitPrice;

    private String rentPriceWithDate;


    private String saleAmountWithDate;

    private String imgUrl;


    private String gdLng;


    private String gdLat;

    private String distance;

    private Integer commercialFloorMin;


    private Integer commercialFloorMax;

}
