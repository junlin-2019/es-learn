package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 10:45
 */
@Slf4j
@Configuration
public class ElasticsearchConfig implements FactoryBean<RestHighLevelClient>, InitializingBean, DisposableBean {

    @Value("${elasticsearch.rest.uri}")
    private String uri;

    private RestHighLevelClient client;

    @Override
    public void destroy() throws Exception {
        if(client!=null){
            client.close();
        }
    }

    @Override
    public RestHighLevelClient getObject() throws Exception {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return RestHighLevelClient.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initClient();
    }

    private void initClient(){
        if (StringUtils.isEmpty(uri)) {
            throw new RuntimeException("elasticsearch uri is unset to properties file");
        }
        String[] nodes = uri.split(",");
        HttpHost[] httpHosts = new HttpHost[nodes.length];
        for (int x = 0; x < nodes.length; x++) {
            String[] uris = nodes[x].split(":");
            HttpHost httpHost = new HttpHost(uris[0], Integer.parseInt(uris[1]), "http");
            httpHosts[x] = httpHost;
        }
        client = new RestHighLevelClient(RestClient.builder(httpHosts).setMaxRetryTimeoutMillis(60000));
    }
}
