package com.example.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 17:49
 */
@Data
public class DemoDto {

    private Integer number;

    List<DemoDetailDto> demoDetailDtos;

    List<DemoComareaCountDto> demoComareaCountDtos;

}
