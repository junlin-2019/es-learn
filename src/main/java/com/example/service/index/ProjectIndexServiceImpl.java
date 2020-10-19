package com.example.service.index;

import com.example.dto.ProjectDto;
import com.example.enums.SyncDataStatusEnum;
import com.example.service.docs.ProjectDetailDoc;
import com.example.service.docs.ProjectDoc;
import com.example.service.docs.ProjectTypeDoc;
import com.example.service.es.IEsService;
import com.example.utils.GenerateHanzi;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/19 14:35
 */
@Service
@Slf4j
public class ProjectIndexServiceImpl implements ProjectIndexService{

    @Autowired
    private IEsService esService;

    @Override
    public boolean index(String indexName, ProjectDto projectDto) {
        return false;
    }

    /**
     * private String address;
     *     private String cityId;
     *     private BigDecimal area;
     *     private Set<ProjectTypeDoc> typeList;
     *     private String developerId;
     *     private BigDecimal price;
     *     private BigDecimal lat;
     *     private BigDecimal lng;
     *     private String desc;
     *     private Set<ProjectDetailDoc> detailList;
     * @param indexName
     * @return
     */

    @Override
    public SyncDataStatusEnum syncFromDataBase(String indexName) {
        List<ProjectDoc> list = new ArrayList<>();
        ArrayList<String> citys = Lists.newArrayList("city01", "city02", "city03", "city04");
        ArrayList<String> names = Lists.newArrayList("皇马国际", "丹凤丽舍", "财富世家", "香溪美地");
        ArrayList<String> openStatus = Lists.newArrayList("0", "1", "2", "3","4","5","6");
        ArrayList<ProjectTypeDoc> typeList = Lists.newArrayList(new ProjectTypeDoc(0,"商场"), new ProjectTypeDoc(1,"住宅"), new ProjectTypeDoc(2,"公寓"));
        ArrayList<ProjectDetailDoc> detailList = Lists.newArrayList(new ProjectDetailDoc(0,"麦当劳",1), new ProjectDetailDoc(1,"肯德基",1), new ProjectDetailDoc(2,"必胜客",1),
                new ProjectDetailDoc(3,"瑞兴",2),new ProjectDetailDoc(4,"星巴克",2),new ProjectDetailDoc(3,"一点点",2));

        for (int i = 0; i < 200; i++) {
            ProjectDoc projectDoc = new ProjectDoc();
            projectDoc.setId(Long.valueOf(i));
            projectDoc.setName(names.get(i%4)+i);
            projectDoc.setOpenStatus(openStatus.get(i%6)+1);
            projectDoc.setAddress("address"+ i);
            projectDoc.setCityId(citys.get(i%4) +i);
            projectDoc.setArea(BigDecimal.valueOf(new Random().nextInt(100)));
            final int temp = i;
            projectDoc.setTypeList(Sets.newHashSet(typeList.stream().filter(t -> t.getId() < temp %3).collect(Collectors.toList())));
            projectDoc.setDeveloperId("developID" + i);
            projectDoc.setPrice(BigDecimal.valueOf(i+ 0.5));
            projectDoc.setLat(BigDecimal.valueOf(39.6577034 + i*0.1));
            projectDoc.setLng(BigDecimal.valueOf(116.0710449 + i*0.1));
            projectDoc.setDesc(GenerateHanzi.getRandomJianHan(20));
            projectDoc.setDetailList(Sets.newHashSet(detailList.stream().filter(t -> t.getId() == temp %3).collect(Collectors.toList())));
            list.add(projectDoc);
        }

        SyncDataStatusEnum syncDataStatusEnum = esService.insertByTask(indexName, list);
        return syncDataStatusEnum;
    }
}
