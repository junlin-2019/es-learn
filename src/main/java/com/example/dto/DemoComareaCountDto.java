package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 18:18
 */
@Data
public class DemoComareaCountDto {


    private Integer projectNum;


    private String comarea;


    private Integer comareaId;


    private String centerLat;


    private String centerLng;

    private String rentUnitPrice;

}
