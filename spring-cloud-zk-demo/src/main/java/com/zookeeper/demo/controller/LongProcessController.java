package com.zookeeper.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author: wenhongliang
 */
@RestController
public class LongProcessController {

    @GetMapping("/long-process")
    public String pause(@RequestParam(value = "num") int num) throws InterruptedException {
        System.out.println(num + "-Process Begin at " + new Date());
        Thread.sleep(3 * 1000);
        System.out.println(num + "-Process finished at " + new Date());
        return "Process finished" + num;
    }
}
