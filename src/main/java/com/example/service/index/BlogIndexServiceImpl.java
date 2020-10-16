package com.example.service.index;

import com.example.dto.BlogDto;
import com.example.enums.SyncDataStatusEnum;
import com.example.service.docs.BlogDoc;
import com.example.service.es.IEsService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 14:15
 */
@Service
public class BlogIndexServiceImpl implements BlogIndexService{
    @Autowired
    private IEsService esService;

    @Override
    public boolean index(String indexName,BlogDto blogDto) {
        BlogDoc blogDoc = new BlogDoc();
        blogDoc.setBlogId(blogDto.getBlogId());
        blogDoc.setAuthor(blogDto.getAuthor());
        blogDoc.setBlogName(blogDto.getBlogName());
        blogDoc.setContent(blogDto.getContent());
        SyncDataStatusEnum syncDataStatusEnum =  esService.insertSingle(indexName,blogDoc);
        if(SyncDataStatusEnum.success.equals(syncDataStatusEnum)){
            return true;
        }
        return false;
    }

    @Override
    public SyncDataStatusEnum syncFromDataBase(String indexName) {
        //模拟从数据库取到数据
        BlogDoc blogDoc1 = new BlogDoc(1l,"git使用","张三","点击右侧个人头像及用户名处,点击Settings选项。在右侧页面顶部的Tab选项中点击SSH Keys菜单。将上一步骤生成的ssh公钥(.pub结尾的文件)用记事本打开，把文件里面的内容粘贴到Key的输入项");
        BlogDoc blogDoc2 = new BlogDoc(2l,"共产党万岁","王三","这样处理的确可以解决问题，但是无疑加大了后端的处理逻辑。你真的不怕后端程序员打你");
        BlogDoc blogDoc3 = new BlogDoc(3l,"我爱你中国","周星驰","在实际工作中往往会有这种情景出现：比如说我需要展示一个游戏名的列表，可接口却会把游戏的详细玩法，更新时间，创建者等各种各样的 （无用的） 信息都一同返回");
        BlogDoc blogDoc4 = new BlogDoc(4l,"我的家乡","权威","一看到用于API的查询语言，我也是一脸懵逼的。博主你在开玩笑吧？你的翻译水平不过关？API还能查吗？API不是后端写好，前端调用的吗");
        BlogDoc blogDoc5 = new BlogDoc(5l,"中国很美丽","豆腐干","说到这里有些同学可能还不满足，如果我想每次查询都想带上一个参数该怎么办，如果我想查询结果有多条数据又怎么处理");
        List<BlogDoc> blogDocs = Lists.newArrayList(blogDoc1, blogDoc2, blogDoc3, blogDoc4, blogDoc5);
        SyncDataStatusEnum syncDataStatusEnum = esService.insertByTask(indexName, blogDocs);
        return syncDataStatusEnum;
    }
}
