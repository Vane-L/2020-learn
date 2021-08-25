package com.test.demo.utils;


import com.alibaba.fastjson.JSONObject;
import com.test.demo.config.TimerConfig;
import com.test.demo.timing.template.BusinessTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

public class TaskUtil {
    private static Logger logger = LoggerFactory.getLogger(TaskUtil.class);

    /***
     * 执行任务,校验是否执行成功
     * @param businessTask 任务内容[1:成功][0:失败]
     * @return
     */
    private static boolean execTask(final BusinessTask businessTask) {
        //如果为空认为是无效任务自动认为执行成功
        if (businessTask == null) {
            return true;
        }
        //如果url为空则任务是无效任务,自动认为执行成功
        if (StringUtils.isEmpty(businessTask.getUrl())) {
            return true;
        }
        JSONObject result = execTaskToJson(businessTask);
        if (result == null) {
            return false;
        }
        String ret = result.getString(TimerConfig.getTimerConfig().getResultCodeName());
        //logger.info("ret == " + ret);
        if (StringUtils.isEmpty(ret)) {
            return false;
        }
        if (!ret.equals("ok")) {
            logger.info(JSONObject.toJSONString(result));
            return false;
        }
        return true;
    }

    /***
     * 执行任务
     * @param businessTask 任务
     */
    public static boolean execTaskAsync(final BusinessTask businessTask) {
        return execTask(businessTask);
    }


    public static JSONObject execTaskToJson(BusinessTask businessTask) {
        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject) JSONObject.parse(
                    httpDoService(businessTask.getUrl(), businessTask.getParams(), businessTask.getRequestMethod()));
            jsonObject.put("id", businessTask.getId());
        } catch (Exception e) {
            return null;
        }
        return jsonObject;
    }

    private static String httpDoService(String url, Map<String, String> params, String requestMethod) {
        String result;
        if (requestMethod.equals(TimerConfig.GET)) {
            result = "{\"ret\":\"ok\"}";
            //result = HttpClientUtil.doGet(url, params, timerConfig.getEncode());
            return result;
        } else if (requestMethod.equals(TimerConfig.POST)) {
            result = "{\"ret\":\"failed\"}";
            //result = HttpClientUtil.doPost(url, params, timerConfig.getEncode());
            return result;
        } else {
            //logger.info("result = " + result);
            return null;
        }
    }
}
