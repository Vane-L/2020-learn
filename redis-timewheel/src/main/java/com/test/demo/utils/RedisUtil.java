package com.test.demo.utils;


import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    private static final RedisUtil redisUtil = new RedisUtil();

    public static RedisUtil getRedisUtil() {
        return redisUtil;
    }

    /***
     * Redis服务器IP
     */
    private String address;

    /***
     * Redis的端口号
     */
    private int port;

    /***
     * 访问密码
     */
    private String auth;

    /***
     * 可用连接实例的最大数目，默认值为8；
     * 如果赋值为-1，则表示不限制；
     * 如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted
     */
    private int maxActive;

    /***
     * 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
     */
    private int maxIdle;

    /***
     * 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
     */
    private int maxWait;

    private int timeOut;

    /***
     * 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
     */
    private boolean testOnBorrow;

    /***
     * 非切片连接池
     */
    private JedisPool jedisPool;

    private RedisUtil() {
        this.address = "localhost";
        this.port = 6379;
        this.maxIdle = 200;
        this.maxActive = 1024;
        this.maxWait = 10000;
        this.timeOut = 10000;
        this.testOnBorrow = true;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxActive);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWait);
        config.setTestOnBorrow(testOnBorrow);
        this.jedisPool = new JedisPool(config, address, port, maxWait, auth);
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public String getAuth() {
        return this.auth;
    }

    public int getMaxActive() {
        return this.maxActive;
    }

    public int getMaxIdle() {
        return this.maxIdle;
    }

    public int getMaxWait() {
        return this.maxWait;
    }

    public int getTimeOut() {
        return this.timeOut;
    }

    public boolean isTestOnBorrow() {
        return this.testOnBorrow;
    }

    public JedisPool getJedisPool() {
        return this.jedisPool;
    }

    /*public static void main(String[] args) {
        RedisUtil redisUtil = new RedisUtil();
        Jedis jedis = redisUtil.getJedisPool().getResource();
        System.out.println(TimerConfig.getTimerConfig().getChannel());
        long len = jedis.llen(TimerConfig.getTimerConfig().getChannel());
        System.out.println(len);
    }*/
}
