package com.example.service.docs;

import com.example.service.es.EsDoc;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 18:08
 */
@Data
public class ProjectDoc  implements EsDoc {
    private Long id;
    private String name;
    private String openStatus;
    private String address;
    private String cityId;
    private BigDecimal area;
    private Set<ProjectTypeDoc> typeList;
    private String developerId;
    private BigDecimal price;
    private BigDecimal lat;
    private BigDecimal lng;
    private String location;
    private String desc;
    private Set<ProjectDetailDoc> detailList;

    @Override
    public String docId() {
        return String.valueOf(id);
    }

    public String getLocation() {
        if (this.getLat() != null && this.getLng() != null) {
            return this.getLat() + "," + this.getLng();
        } else {
            return null;
        }
    }
}
