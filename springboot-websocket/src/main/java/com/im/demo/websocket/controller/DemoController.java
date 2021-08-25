package com.im.demo.websocket.controller;

import com.im.demo.websocket.server.WebSocketServer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.websocket.EncodeException;
import java.io.IOException;

/**
 * @Author: wenhongliang
 */
@RestController
public class DemoController {
    @GetMapping("index")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("请求成功");
    }

    @GetMapping("page")
    public ModelAndView page() {
        return new ModelAndView("websocket");
    }

    @GetMapping("/push/{toUserId}")
    public ResponseEntity<String> pushToWeb(String message, @PathVariable String toUserId) throws IOException, EncodeException {
        WebSocketServer.sendInfo(message, toUserId);
        return ResponseEntity.ok("MSG SEND SUCCESS");
    }
}
