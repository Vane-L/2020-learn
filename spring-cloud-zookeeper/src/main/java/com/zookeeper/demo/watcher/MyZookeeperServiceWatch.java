package com.zookeeper.demo.watcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceWatch;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: wenhongliang
 */
@Component
@Slf4j
public class MyZookeeperServiceWatch extends ZookeeperServiceWatch {

    @Autowired
    private SpringClientFactory springClientFactory;

    public MyZookeeperServiceWatch(CuratorFramework curator, ZookeeperDiscoveryProperties properties) {
        super(curator, properties);
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        super.childEvent(client, event);
        if (event.getType().equals(TreeCacheEvent.Type.NODE_ADDED)
                || event.getType().equals(TreeCacheEvent.Type.NODE_REMOVED)
                || event.getType().equals(TreeCacheEvent.Type.NODE_UPDATED)) {
            ChildData data = event.getData();
            if (data == null) {
                log.info("data is null");
                return;
            }
            String dataStr = new String(data.getData(), "UTF-8");
            clearCache(dataStr);
            log.info("[Event]{}, data={}", event.getType(), dataStr);
        }
    }

    private void clearCache(String dataStr) {
        if (StringUtils.isBlank(dataStr)) {
            log.info("data is null");
            return;
        }
        String name = getAsJsonObject(dataStr, "name").textValue();
        DynamicServerListLoadBalancer dynamicServerListLoadBalancer = (DynamicServerListLoadBalancer) springClientFactory.getLoadBalancer(name);
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
