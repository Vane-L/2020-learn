package com.test.demo.message;


import com.alibaba.fastjson.JSONObject;
import com.test.demo.constant.RedisKey;
import com.test.demo.utils.MessageUtil;
import com.test.demo.config.TimerConfig;
import com.test.demo.timing.template.BusinessTask;
import com.test.demo.utils.TaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 消费者执行任务
 */
public class ConsumerCarriedOutTask extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ConsumerCarriedOutTask.class);

    private Jedis jedis;

    /***
     * 时间轮配置文件
     */
    private final static TimerConfig timerConfig = TimerConfig.getTimerConfig();

    /***
     * 失败次数上限
     */
    private final int failuresNumberCapped = 5;

    /***
     * 垃圾回收队列对象
     */
    // private TaskRecycleBinList taskRecycleBinList;

    /***
     * 备用频道
     */
    private String channel;

    /***
     * 备用频道
     */
    private String channelTmp;

    public ConsumerCarriedOutTask(JedisPool jedisPool, int name) {
        super(String.valueOf(name));
        this.jedis = jedisPool.getResource();
        this.channel = timerConfig.getChannel();
        this.channelTmp = timerConfig.getChannelTmp();
        // this.taskRecycleBinList = new TaskRecycleBinList(jedisPool);
    }

    public void run() {
        logger.info("执行任务消费者启动---");
        while (true) {
            try {
                if ("OK".equals(jedis.setex(RedisKey.CHANNEL_TMP_LOCK, 1, RedisKey.CHANNEL_TMP_LOCK))) {
                    String task = jedis.brpoplpush(this.channel, this.channelTmp, 1);
                    if (task == null || task.length() == 0) {
                        continue;
                    }
                    BusinessTask businessTask = MessageUtil.getObjectFromString(task, BusinessTask.class);
                    boolean bool = TaskUtil.execTaskAsync(businessTask);
                    logger.info("任务执行结果" + bool + "--->任务详细:" + JSONObject.toJSONString(businessTask));
                    //将结果存入redis中
                    if (bool) {//成功则删除此任务
                        jedis.rpop(this.channelTmp);
                    } else {//未成功则判断这个任务失败了多少次,如果超过上限则添加到任务回收站队列
                        String taskId = businessTask.getId();
                        long failuresNumber = jedis.incr(taskId);
                        if (failuresNumber > this.failuresNumberCapped) {
                            //添加到任务回收站队列
                            //this.taskRecycleBinList.setFailureTask();
                            //jedis.rpop(this.channelTmp);
                            logger.info(String.format("失败次数超过%d进入失败任务回收队列--->%s", failuresNumberCapped, task));
                        } else {
                            jedis.rpoplpush(this.channelTmp, this.channel);
                        }
                    }
                    //弹回执行队列,tmp队列按逻辑上讲长度应该时刻保持0-1的长度
                    boolean reboundBool = true;
                    while (reboundBool) {
                        String reboundTask = jedis.rpoplpush(this.channelTmp, this.channel);
                        if (reboundTask == null || reboundTask.length() == 0) {
                            reboundBool = false;
                        }
                    }
                    jedis.del(RedisKey.CHANNEL_TMP_LOCK);
                } else {
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /***
     * 添加一个消息任务
     * @param task
     */
    public void setTask(String task) {
        jedis.rpush(this.channel, task);
    }

    public static void main(String[] args) {
        String json = "{\"carriedTurns\":0,\"cycle\":false,\"id\":\"dfdf968c-1cc9-4f0d-a1f9-1f48a073422b\",\"initialDelay\":{\"milliSecond\":4500000},\"params\":{\"timerToken\":\"BB47501E646085AB96767DA1A74D51493BE19106073D152157280551DB6B1AAF\",\"accountId\":\"53533\",\"testunitId\":\"16919\",\"answerId\":\"645323\"},\"requestMethod\":\"POST\",\"slot\":8,\"url\":\"http://admin.hushijie.com.cn/testunit/student/transcript/markzero\"}";
        BusinessTask businessTask = JSONObject.parseObject(json, BusinessTask.class);
        System.out.println("json =" + JSONObject.toJSONString(businessTask));
        System.out.println(TaskUtil.execTaskAsync(businessTask));
    }
}
