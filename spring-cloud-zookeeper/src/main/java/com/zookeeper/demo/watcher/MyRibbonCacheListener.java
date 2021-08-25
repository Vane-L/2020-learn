package com.zookeeper.demo.watcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @Author: wenhongliang
 */
@Slf4j
@Component
public class MyRibbonCacheListener implements ApplicationRunner {

    @Autowired
    private CuratorFramework curator;

    private ConfigurableApplicationContext context;

    private SpringClientFactory springClientFactory;

    @Autowired
    public void setContext(ConfigurableApplicationContext context) {
        this.context = context;
        this.springClientFactory = this.context.getBean(SpringClientFactory.class);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TreeCache treeCache = new TreeCache(curator, "/services");
        TreeCacheListener l = (curatorFramework, event) -> {
            try {
                ChildData data = event.getData();
                if (data == null) {
                    log.info("data is null");
                    return;
                }
                String dataStr = new String(data.getData(), "UTF-8");
                //clearCache(dataStr);
                switch (event.getType()) {
                    case NODE_ADDED:
                        log.info("[TreeCache]Node add, path={}, data={}", data.getPath(), dataStr);
                        break;
                    case NODE_UPDATED:
                        log.info("[TreeCache]Node update, path={}, data={}", data.getPath(), dataStr);
                        break;
                    case NODE_REMOVED:
                        log.info("[TreeCache]Node delete, path={}, data={}", data.getPath(), dataStr);
                        break;
                    default:
                        break;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        };
        treeCache.getListenable().addListener(l);
        treeCache.start();
        log.info("start success!");
        String s = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        log.info("$$$$$$$$classpath => " + s);
    }

    private void clearCache(String dataStr) {
        if (StringUtils.isBlank(dataStr)) {
            log.info("data is null");
            return;
        }
        String name = getAsJsonObject(dataStr, "name").textValue();
        DynamicServerListLoadBalancer dynamicServerListLoadBalancer =
                (DynamicServerListLoadBalancer) springClientFactory.getLoadBalancer(name);
        dynamicServerListLoadBalancer.updateListOfServers();
        log.info(">>> {} -- {}", dynamicServerListLoadBalancer.getLastUpdate(), dynamicServerListLoadBalancer.getAllServers());
    }

    private JsonNode getAsJsonObject(String json, String key) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(json);
            if (null == node) {
                return null;
            }
            return node.get(key);
        } catch (IOException e) {
            log.error("jackson get object from json error, json: {}, key: {}", json, key, e);
            throw new IllegalArgumentException(String.format("jackson get object from json error, json: [%s], key: [%s]", json, key));
        }
    }
}
