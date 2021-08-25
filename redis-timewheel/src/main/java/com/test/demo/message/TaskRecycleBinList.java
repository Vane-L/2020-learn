package com.test.demo.message;

import com.test.demo.constant.RedisKey;
import com.test.demo.utils.MessageUtil;
import com.test.demo.config.TimerConfig;
import com.test.demo.timing.template.BusinessTask;
import com.test.demo.utils.TaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 任务回收列表
public class TaskRecycleBinList extends Thread {
    private static Logger logger = LoggerFactory.getLogger(TaskRecycleBinList.class);

    /***
     * redis连接对象
     */
    private Jedis jedis;


    /***
     * 失败任务执行循环间隔单位:秒
     * 12小时
     */
    private int cycleInterval = (int) TimeUnit.SECONDS.toMillis(43200);

    /***
     * 失败任务获取最大长度
     */
    private long taskLength = 500;

    public TaskRecycleBinList(JedisPool jedisPool) {
        this.jedis = jedisPool.getResource();
    }

    @Override
    public void run() {
        while (true) {
            try {
                String ok = jedis.setex(RedisKey.TASK_RECYCLE_LOCK, cycleInterval, RedisKey.TASK_RECYCLE_LOCK);
                if ("OK".equals(ok)) {
                    boolean bool = true;
                    while (bool) {
                        logger.info("开始执行失败队列");
                        List<String> taskList = this.getTasks();
                        List<String> failureTaskList = new ArrayList<>();
                        for (String task : taskList) {
                            boolean taskBool = TaskUtil.execTaskAsync(MessageUtil.getObjectFromString(task, BusinessTask.class));
                            if (!taskBool) {//如果没成功则追加到队列后面
                                failureTaskList.add(task);
                            }
                        }
                        int failureTaskListSize = failureTaskList.size();
                        int taskListSize = taskList.size();
                        this.removeTasks(failureTaskList.toArray(new String[failureTaskListSize]), taskListSize);
                        logger.info(String.format("失败队列执行完毕:共执行%d个任务,其中失败了%d个任务", taskList.size(), failureTaskListSize));
                        bool = false;
                    }
                } else {
                    sleep(10000);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /***
     * 获取当前失败任务队列中的所有任务
     */
    public List<String> getTasks() {
        long length = this.jedis.llen(RedisKey.TASK_RECYCLE);
        if (length > this.taskLength) {//只取前规定的长度的失败任务
            length = this.taskLength;
        }
        return this.jedis.lrange(RedisKey.TASK_RECYCLE, 0, length);
    }

    /***
     * 装载又执行失败的任务
     * @param task 又执行失败的任务
     * @param count 获取对象的范围
     * @return
     */
    public void removeTasks(String[] task, long count) {
        Transaction transaction = jedis.multi();
        if (task.length != 0) {
            transaction.rpush(RedisKey.TASK_RECYCLE, task);
        }
        transaction.ltrim(RedisKey.TASK_RECYCLE, count, -1);
        transaction.exec();
    }

    /***
     * 单个装载失败任务
     */
    public void setFailureTask() {
        jedis.rpoplpush(TimerConfig.getTimerConfig().getChannelTmp(), RedisKey.TASK_RECYCLE);
    }

    /*public static void main(String[] args) {
        System.out.println(TimeUnit.HOURS.toSeconds(1));
    }*/
}
