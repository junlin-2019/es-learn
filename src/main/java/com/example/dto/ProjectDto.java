package com.example.dto;

import com.example.service.docs.ProjectDetailDoc;
import com.example.service.docs.ProjectTypeDoc;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/19 11:10
 */
@Data
public class ProjectDto {
    private Integer id;
    private String name;
    private String openStatus;
    private String address;
    private String cityId;
    private String districtId;
    private BigDecimal area;
    private Set<ProjectTypeDoc> typeList;
    private String developerId;
    private BigDecimal price;
    private BigDecimal lat;
    private BigDecimal lng;
    private String desc;
    private Set<ProjectDetailDoc> detailList;
    private Double distince;
}
