package com.test.demo.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;


public class TimerConfig {
    private static Logger logger = LoggerFactory.getLogger(TimerConfig.class);
    private static final TimerConfig timerConfig = new TimerConfig();

    /***
     * 线程数量
     */
    private int corePoolSize;

    /***
     * 请求编码格式
     */
    private String encode;

    /***
     * 校验token(哈希值)
     */
    private String token;

    /***
     * 测试环境ip地址
     */
    private String testIp;

    /***
     * 执行器生产消费者地址(执行队列)
     */
    private String channel;

    /***
     * 执行器生产消费者地址(备份队列,如果执行队列执行失败,则从备份队列中重新获取,并添加到执行队列)
     */
    private String channelTmp;

    /***
     * 槽的数量
     */
    private int grooveNumber;

    /***
     * 在容器中存储的key值前缀
     */
    private String keyPrefix;

    /***
     * 线程最大数量
     */
    private int ringCorePoolSize;

    /***
     * 循环时距离上次执行开始时的间隔
     */
    private int initialDelay;

    /***
     * 指针锁的key值
     */
    private String lockKey;

    /***
     * 指针当前所指向的地址,从redis中获取(移动指针时必须为原子操作),还需要在项目启动时定位上次指针的位置
     */
    private String pointer;

    /***
     * 遍历环形结构的时间间隔
     */
    private int period;

    /***
     * 锁的时间
     */
    private int lockOutTime;

    /***
     * 发布任务消费消费者地址
     */
    private String hashedWheelProducerTask;

    /***
     * 发布任务消费消费者备用队列地址
     */
    private String hashedWheelProducerTaskTmp;

    /****
     * 判断任务是否执行成功的字段的名称
     */
    private String resultCodeName;

    /***
     * 时间单位:秒
     */
    public static final TimeUnit timeUnit = TimeUnit.SECONDS;

    /***
     * post请求
     */
    public static final String POST = "POST";

    /***
     * get请求
     */
    public static final String GET = "GET";

    public static TimerConfig getTimerConfig() {
        return TimerConfig.timerConfig;
    }

    private TimerConfig() {
        this.corePoolSize = 10;
        this.encode = "UTF-8";
        this.token = "123456";
        this.testIp = "localhost";
        this.channel = "hashed-wheel-pointer-key-1";
        this.channelTmp = "hashed-wheel-tmp-1";
        this.grooveNumber = 10;
        this.keyPrefix = "hashed-wheel-key-prefix-";
        this.ringCorePoolSize = 10;
        this.initialDelay = 0;
        this.lockKey = "hashed-wheel-lock-key";
        this.pointer = "hashed-wheel-pointer-address";
        this.lockOutTime = 3;
        this.period = 1;
        this.hashedWheelProducerTask = "hashed-wheel-producer-task";
        this.hashedWheelProducerTaskTmp = "hashed-wheel-producer-task-tmp";
        this.resultCodeName = "ret";
        this.isIp();
    }

    private void isIp() {
        String testIp = this.testIp;
        //判断是否为测试环境或者本地
        if (testIp == null || testIp.length() == 0) {
            return;
        }
        //判断是否在本地运行
        String localIp = this.getLocalIp();
        logger.info("localIp = " + localIp);
        if (localIp != null) {//判定为本地的话则生成隔离环境地址
            this.channel = localIp + this.channel;
            this.channelTmp = localIp + this.channelTmp;
            this.keyPrefix = localIp + this.keyPrefix;
            this.lockKey = localIp + this.lockKey;
            this.pointer = localIp + this.pointer;
            this.hashedWheelProducerTask = localIp + this.hashedWheelProducerTask;
        }
    }

    private String getLocalIp() {
        Enumeration<NetworkInterface> n = null;
        try {
            n = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (n.hasMoreElements()) {
            NetworkInterface e = n.nextElement();
            Enumeration<InetAddress> a = e.getInetAddresses();
            if (!e.getName().equals("en1")) {
                continue;
            }
            for (; a.hasMoreElements(); ) {
                String ip = a.nextElement().getHostAddress();
                String[] ips = ip.split("\\.");
                if (ips.length == 4) {
                    return ip;
                }
            }
        }
        return null;
    }

    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    public String getEncode() {
        return this.encode;
    }

    public String getToken() {
        return this.token;
    }

    public String getTestIp() {
        return this.testIp;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getChannelTmp() {
        return this.channelTmp;
    }

    public int getGrooveNumber() {
        return this.grooveNumber;
    }

    public String getKeyPrefix() {
        return this.keyPrefix;
    }

    public int getRingCorePoolSize() {
        return this.ringCorePoolSize;
    }

    public int getInitialDelay() {
        return this.initialDelay;
    }

    public String getLockKey() {
        return this.lockKey;
    }

    public String getPointer() {
        return this.pointer;
    }

    public int getPeriod() {
        return this.period;
    }

    public int getLockOutTime() {
        return this.lockOutTime;
    }

    public String getHashedWheelProducerTask() {
        return this.hashedWheelProducerTask;
    }

    public String getResultCodeName() {
        return this.resultCodeName;
    }

    public String getHashedWheelProducerTaskTmp() {
        return this.hashedWheelProducerTaskTmp;
    }
}
