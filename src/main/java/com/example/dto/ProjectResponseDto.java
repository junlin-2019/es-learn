package com.example.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:49
 */
@Data
public class ProjectResponseDto {

    private Integer number;

    List<ProjectDto> projectDtos;

    List<CityMaxDto> cityMaxDtos;

}
