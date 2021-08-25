package com.cache.demo.limit;

import com.google.common.util.concurrent.RateLimiter;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @Author: wenhongliang
 */
public class TestRateLimit {
    // 每秒可以处理的请求数
    public static final RateLimiter limiter = RateLimiter.create(5.0);

    public static void submitTask() {
        String start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        for (int i = 0; i < 10; i++) {
            // 从RateLimiter获取一个许可，该方法会被阻塞直到获取到请求
            limiter.acquire();
            System.out.println("count => " + i);
        }
        String end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println(start + " => " + end); // 2秒完成
    }

    public static void main(String[] args) {
        submitTask();
    }
}
