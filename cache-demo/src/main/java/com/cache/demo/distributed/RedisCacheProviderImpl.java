package com.cache.demo.distributed;

import com.cache.demo.CacheProviderService;
import com.cache.demo.constant.AppConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author: wenhongliang
 */
@Configuration
@ComponentScan(basePackages = AppConst.BASE_PACKAGE_NAME)
@Qualifier("redisCacheService")
public class RedisCacheProviderImpl implements CacheProviderService {

    @Resource
    private RedisTemplate<Serializable, Object> redisTemplate;

    @Override
    public <T> T get(String key) {
        T obj = get(key, null, null, AppConst.CACHE_MINUTE);
        return obj;
    }

    @Override
    public <T> T get(String key, Function<String, T> function) {
        T obj = get(key, function, null, AppConst.CACHE_MINUTE);
        return obj;
    }

    @Override
    public <T, M> T get(String key, Function<M, T> function, M funcParam) {
        T obj = get(key, function, funcParam, AppConst.CACHE_MINUTE);
        return obj;
    }

    @Override
    public <T> T get(String key, Function<String, T> function, Long expireTime) {
        T obj = get(key, function, null, expireTime);
        return obj;
    }

    @Override
    public <T, M> T get(String key, Function<M, T> function, M funcParam, Long expireTime) {
        if (StringUtils.isEmpty(key) == true) {
            return null;
        }

        T obj = null;
        expireTime = getExpireTime(expireTime);
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            obj = (T) operations.get(key);
            if (function != null && obj == null) {
                obj = function.apply(funcParam);
                if (obj != null) {
                    set(key, obj, expireTime);//设置缓存信息
                }
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
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        operations.set(key, obj);
        redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
    }

    public void remove(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        redisTemplate.delete(key);
    }


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
}
