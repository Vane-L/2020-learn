package com.cache.demo.local;

import com.cache.demo.CacheProviderService;
import com.cache.demo.constant.AppConst;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * @Author: wenhongliang
 */
@Configuration
@ComponentScan(basePackages = AppConst.BASE_PACKAGE_NAME)
@Qualifier("localCacheService")
public class LocalCacheProviderImpl implements CacheProviderService {

    private static Map<String, Cache<String, Object>> _cacheMap = Maps.newConcurrentMap();

    /**
     * expireAfterWrite：最后一次写入后的一段时间移出。
     * expireAfterAccess：最后一次访问后的一段时间移出。
     */
    static {
        Cache<String, Object> cacheContainer = CacheBuilder.newBuilder()
                .maximumSize(AppConst.CACHE_MAXIMUM_SIZE)
                .expireAfterWrite(AppConst.CACHE_MINUTE, TimeUnit.MILLISECONDS) // 最后一次写入后的一段时间移出
                //.expireAfterAccess(AppConst.CACHE_MINUTE, TimeUnit.MILLISECONDS) // 最后一次访问后的一段时间移出
                .recordStats() // 开启统计功能
                .build();
        _cacheMap.put(String.valueOf(AppConst.CACHE_MINUTE), cacheContainer);
    }


    @Override
    public <T> T get(String key) {
        T obj = get(key, null, null, AppConst.CACHE_MINUTE);
        return obj;
    }

    @Override
    public <T> T get(String key, Function<String, T> function) {
        T obj = get(key, function, key, AppConst.CACHE_MINUTE);
        return obj;
    }

    @Override
    public <T, M> T get(String key, Function<M, T> function, M funcParm) {
        T obj = get(key, function, funcParm, AppConst.CACHE_MINUTE);
        return obj;
    }

    @Override
    public <T> T get(String key, Function<String, T> function, Long expireTime) {
        T obj = get(key, function, key, expireTime);
        return obj;
    }

    @Override
    public <T, M> T get(String key, Function<M, T> function, M funcParam, Long expireTime) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        T obj = null;
        expireTime = getExpireTime(expireTime);
        Cache<String, Object> cacheContainer = getCacheContainer(expireTime);
        try {
            if (function == null) {
                obj = (T) cacheContainer.getIfPresent(key);
            } else {
                obj = (T) cacheContainer.get(key, () -> {
                    T retObj = function.apply(funcParam);
                    return retObj;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public <T> void set(String key, T obj) {
        set(key, obj, AppConst.CACHE_MINUTE);
    }

    @Override
    public <T> void set(String key, T obj, Long expireTime) {
        if (StringUtils.isEmpty(key) || obj == null) {
            return;
        }
        expireTime = getExpireTime(expireTime);
        Cache<String, Object> cacheContainer = getCacheContainer(expireTime);
        cacheContainer.put(key, obj);
    }

    @Override
    public void remove(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        long expireTime = getExpireTime(AppConst.CACHE_MINUTE);
        Cache<String, Object> cacheContainer = getCacheContainer(expireTime);
        cacheContainer.invalidate(key);
    }

    @Override
    public boolean contains(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        boolean exists = false;
        Object obj = get(key);
        if (obj != null) {
            exists = true;
        }
        return exists;
    }

    private Long getExpireTime(Long expireTime) {
        Long result = expireTime;
        if (expireTime == null || expireTime < AppConst.CACHE_MINUTE / 10) {
            result = AppConst.CACHE_MINUTE;
        }
        return result;
    }

    private static Lock lock = new ReentrantLock();

    private Cache<String, Object> getCacheContainer(Long expireTime) {
        Cache<String, Object> cacheContainer = null;
        if (expireTime == null) {
            return cacheContainer;
        }

        String mapKey = String.valueOf(expireTime);
        if (_cacheMap.containsKey(mapKey) == true) {
            cacheContainer = _cacheMap.get(mapKey);
            return cacheContainer;
        }

        try {
            lock.lock();
            cacheContainer = CacheBuilder.newBuilder()
                    .maximumSize(AppConst.CACHE_MAXIMUM_SIZE)
                    .expireAfterWrite(expireTime, TimeUnit.MILLISECONDS)//最后一次写入后的一段时间移出
                    //.expireAfterAccess(AppConst.CACHE_MINUTE, TimeUnit.MILLISECONDS) //最后一次访问后的一段时间移出
                    .recordStats()//开启统计功能
                    .build();
            _cacheMap.put(mapKey, cacheContainer);
        } finally {
            lock.unlock();
        }

        return cacheContainer;
    }

}
