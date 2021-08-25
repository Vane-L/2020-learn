package com.im.demo.multi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.im.demo.model.ForwardSocketUserInfoRequest;
import com.im.demo.model.SocketUserInfoDTO;
import com.im.demo.model.SocketUserInfoSessionDTO;
import com.im.demo.multi.server.MultiWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
@Slf4j
@RestController
@RequestMapping("/websocket")
public class MultiWebSocketController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param request
     * @return
     */
    @RequestMapping(value = "/socketSend", method = RequestMethod.POST)
    @ResponseBody
    String socketSend(@RequestBody ForwardSocketUserInfoRequest request) {
        String redisKey = MultiWebSocketServer.WEB_SOCKET_REDIS_KEY_PREFIX + request.getUserId();
        Object redisSocketObj = redisTemplate.opsForValue().get(redisKey);
        SocketUserInfoDTO socketUserInfoDTO = null;
        if (null != redisSocketObj) {
            String resultStr = redisSocketObj.toString();
            socketUserInfoDTO = JSONObject.parseObject(resultStr, SocketUserInfoDTO.class);
        }
        if (null == socketUserInfoDTO) {
            log.error("给用户推送websocket消息失败");
            return "success";
        }
        Map<String, Map<String, SocketUserInfoSessionDTO>> mapMap = socketUserInfoDTO.getListMap();
        Map<String, SocketUserInfoSessionDTO> sessionDTOMap = mapMap.get(request.getUserId());
        //遍历map,根据Ip通过HTTP方式，传入sessionId参数，调用推送信息接口（这里的接口需要根据sessionId从内存中的map拿到对应的websocket,然后发送消息），推送信息到客户端
        Iterator<Map.Entry<String, SocketUserInfoSessionDTO>> entryIterator = sessionDTOMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, SocketUserInfoSessionDTO> entry = entryIterator.next();
            String ipAndPort = "local";
            if (ipAndPort.equals(entry.getValue().getIp())) {  //如果是本台服务器，直接发送
                try {
                    MultiWebSocketServer.sendInfo(JSON.toJSONString(request.getObject()), request.getUserId(), entry.getValue().getSessionId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                request.setSessionId(entry.getValue().getSessionId());
                //通过HTTP转发到对应的服务器做处理
                //JnHttpUtils.post(JSON.toJSONString(request), "http://" + entry.getValue().getIp() + "/websocket/forwardSend");
            }
        }
        return "success";
    }

    /**
     * 该接口供本服务内部通过转发到对用的服务器发送http调用
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/forwardSend", method = RequestMethod.POST)
    @ResponseBody
    String forwardSend(@RequestBody ForwardSocketUserInfoRequest request) {
        try {
            MultiWebSocketServer.sendInfo(JSON.toJSONString(request.getObject()), request.getUserId(), request.getSessionId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }

}
