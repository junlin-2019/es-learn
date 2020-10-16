package com.example.service.document;

import com.example.enums.SyncDataStatusEnum;
import com.example.exception.IndexDataRuntimeException;
import com.example.service.es.IEsService;
import com.example.service.index.BlogIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 13:17
 */
@Service
@Slf4j
public class BlogDocumentServiceImpl implements BlogDocumentService{

    private static final String INDEX_PREFIX = "hh_blog.";
    private static final String ALIAS = "hh_blog.alias";
    private static final String MAPPING = "{\"dynamic\":\"false\",\"properties\":{\"blogName\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\"},\"author\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\"},\"content\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\"}}}";
    private static final String INDEX_PATTERN = "yyyyMMddHHmmss";


    @Autowired
    private IEsService esService;

    @Autowired
    private BlogIndexService indexService;
    @Override
    public boolean bulidIndexAndInit() {
        //1.生成新索引名称
        String newIndex = INDEX_PREFIX + LocalDateTime.now().format(DateTimeFormatter.ofPattern(INDEX_PATTERN));
        //2.获取历史索引
        List<String> alias = esService.getAlias(ALIAS);
        //3.创建新索引并设置别名
        boolean created = esService.createIndex(newIndex, ALIAS, MAPPING);
        //4.全量同步数据库对应数据
        if (created) {
            SyncDataStatusEnum syncDataStatusEnum = indexService.syncFromDataBase(newIndex);
            //5.同步数据成功则删除所有历史索引
            if (SyncDataStatusEnum.success.equals(syncDataStatusEnum)) {
                boolean result = true;
                if (!CollectionUtils.isEmpty(alias)) {
                    for (String s : alias) {
                        result = result && esService.deleteIndex(s);
                    }
                }
                return result;
            } else if (SyncDataStatusEnum.part_success.equals(syncDataStatusEnum)) {
                esService.addAlias(newIndex, ALIAS);
                throw new IndexDataRuntimeException("部分数据未同步成功");
            } else {
                esService.deleteIndex(newIndex);
                throw new IndexDataRuntimeException("数据同步失败");
            }
        }
        return false;
    }
}
