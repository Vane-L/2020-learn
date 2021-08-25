package com.im.demo.sse.controller;

import com.im.demo.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: wenhongliang
 */
@Slf4j
@CrossOrigin
@RestController
public class SseController {
    private Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();

    @Autowired
    private HttpServletResponse response;

    @ResponseBody
    @GetMapping(path = "subscribe")
    public SseEmitter subscribe(String id) throws IOException {
        /*response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");*/
        SseEmitter emitter = new SseEmitter(3000L);
        emitter.send(SseEmitter.event().reconnectTime(3000L).data("connect success" + new Date()));
        sseCache.put(id, emitter);
        emitter.onTimeout(() -> sseCache.remove(id));
        emitter.onCompletion(() -> log.info("complete!!!"));
        return emitter;
    }

    @ResponseBody
    @GetMapping(path = "push")
    public String pushData(String id, String content) throws IOException {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            sseEmitter.send(new User(content + new Date(), 111));
        }
        return "over!";
    }

    @ResponseBody
    @GetMapping(path = "over")
    public String over(String id) {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            sseEmitter.complete();
            sseCache.remove(id);
        }
        return "over!";
    }
}
