package com.test.demo;

import com.alibaba.fastjson.JSONObject;
import com.test.demo.timing.template.BusinessTask;
import com.test.demo.timing.template.DateToX;
import com.test.demo.utils.RedisUtil;
import com.test.demo.message.ConsumerCarriedOutTask;
import com.test.demo.message.ConsumerProducerTask;
import com.test.demo.message.TaskRecycleBinList;
import com.test.demo.timing.ringlist.TimeRound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TimerStart {
    private static Logger logger = LoggerFactory.getLogger(TimerStart.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("start .....");
        JedisPool jedisPool = RedisUtil.getRedisUtil().getJedisPool();
        int count = 6;

        // 消费者执行任务
        ConsumerCarriedOutTask[] consumerCarriedOutTasks = new ConsumerCarriedOutTask[count];
        for (int i = 0; i < count; i++) {
            consumerCarriedOutTasks[i] = new ConsumerCarriedOutTask(jedisPool, i);
            BusinessTask task = new BusinessTask();
            task.setCycle(true);
            task.setCarriedTurns(i);
            task.setId(UUID.randomUUID().toString());
            task.setInitialDelay(new DateToX(TimeUnit.SECONDS, i + 1));
            if (i % 2 == 0) {
                task.setRequestMethod("GET");
            } else {
                task.setRequestMethod("POST");
            }
            task.setRequestMethod("GET");
            task.setUrl("http://www.baidu.com");
            //logger.info(task.toString());
            consumerCarriedOutTasks[i].setTask(JSONObject.toJSONString(task));
            consumerCarriedOutTasks[i].start();
        }

        // 消费者生产者任务
        ConsumerProducerTask consumerProducerTask = new ConsumerProducerTask(jedisPool);
        consumerProducerTask.start();

        // 任务回收列表
        //TaskRecycleBinList taskRecycleBinList = new TaskRecycleBinList(jedisPool);
        //taskRecycleBinList.start();

        Thread.sleep(2000);

        TimeRound timeRound = new TimeRound(jedisPool);
        timeRound.traverse();
    }
}
