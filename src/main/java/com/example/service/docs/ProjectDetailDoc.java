package com.example.service.docs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 18:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDetailDoc {
    private Integer id;
    private String brand;
    private Integer level;
}
