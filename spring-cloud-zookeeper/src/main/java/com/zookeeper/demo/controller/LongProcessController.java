package com.zookeeper.demo.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Enumeration;


/**
 * @Author: wenhongliang
 */
@RestController
@Slf4j
public class LongProcessController {

    @GetMapping("/long-process")
    public String pause(@RequestParam(value = "num") int num) throws InterruptedException {
        System.out.println(num + "-Process Begin at " + new Date());
        Thread.sleep(3 * 1000);
        System.out.println(num + "-Process finished at " + new Date());
        return "Process finished" + num;
    }

    @GetMapping("/test")
    public Result<String> test(@RequestParam(value = "source", required = false) String source,
                       @RequestParam(value = "wait", required = false) String wait,
                       @RequestParam(value = "number", required = false) String telephone,
                       @RequestParam(value = "text", required = false) String content,
                       HttpServletRequest request) throws InterruptedException {
        System.out.println("sdadasdsadasd");
        Thread.sleep(5000L);
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            log.info("{}:{}", key, value);
        }
        return Result.success("{\"code\": 1, \"message\": \"No valid sender for 628100500019076, the senders attempted were: [infobip_new]\", \"message_id\": \"\"}");
    }
}
