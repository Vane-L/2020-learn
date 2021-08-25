package com.zookeeper.demo.watcher;

import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.zookeeper.discovery.watcher.DependencyState;
import org.springframework.cloud.zookeeper.discovery.watcher.DependencyWatcherListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @Author: wenhongliang
 */
@Component
@Slf4j
public class MyDependencyWatcherListener implements DependencyWatcherListener {

    private ConfigurableApplicationContext applicationContext;

    private SpringClientFactory factory;
    private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;

    @Autowired
    public void setContext(ConfigurableApplicationContext context) {
        this.applicationContext = context;
        this.factory = this.applicationContext.getBean(SpringClientFactory.class);
        this.cachingSpringLoadBalancerFactory = this.applicationContext.getBean(CachingSpringLoadBalancerFactory.class);
    }

    @Override
    public void stateChanged(String dependencyName, DependencyState newState) {
        log.info("stateChanged >>> " + dependencyName + ":" + newState.name());

        // TODO dependencyName (e.g /zk-demo need to substring)
        DynamicServerListLoadBalancer dynamicServerListLoadBalancer = (DynamicServerListLoadBalancer) factory.getLoadBalancer(dependencyName.substring(1));
        dynamicServerListLoadBalancer.updateListOfServers();
        log.info(">>> 1 {} --  {}", dynamicServerListLoadBalancer.getLastUpdate(), dynamicServerListLoadBalancer.getReachableServers());
        log.info(">>> 1 {} -- {}", dynamicServerListLoadBalancer.getLastUpdate(), dynamicServerListLoadBalancer.getAllServers());

        DynamicServerListLoadBalancer dynamicServerListLoadBalancer1 = (DynamicServerListLoadBalancer) cachingSpringLoadBalancerFactory.create(dependencyName.substring(1)).getLoadBalancer();
        dynamicServerListLoadBalancer1.updateListOfServers();
        log.info(">>> 2 {} --  {}", dynamicServerListLoadBalancer.getLastUpdate(), dynamicServerListLoadBalancer1.getReachableServers());
        log.info(">>> 2 {} -- {}", dynamicServerListLoadBalancer.getLastUpdate(), dynamicServerListLoadBalancer1.getAllServers());
    }
}
