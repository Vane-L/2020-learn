package com.jwt.demo;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: wenhongliang
 */

public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token"); // 从 http 请求头中取出 token
        if (token == null) {
            throw new RuntimeException("无token，请重新登录");
        }
        User user = new User("1", "abc", "pass");
        return JwtUtil.verifyToken(token, user);
    }
}
