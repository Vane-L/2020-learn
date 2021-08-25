package com.jwt.demo;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: wenhongliang
 */
@RestController
@RequestMapping("api")
public class UserController {

    @PostMapping("/login")
    public Object login(@RequestBody User user) {
        JSONObject jsonObject = new JSONObject();
        User userForBase = new User("1", "abc", "pass");
        if (!userForBase.getPassword().equals(user.getPassword())) {
            jsonObject.put("message", "登录失败,密码错误");
        } else {
            String token = JwtUtil.getToken(userForBase);
            jsonObject.put("token", token);
            jsonObject.put("user", userForBase);
        }
        return jsonObject;
    }

    @GetMapping("/getMessage")
    public String getMessage() {
        return "你已通过验证";
    }
}
