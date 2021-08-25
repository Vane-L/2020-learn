package com.jwt.demo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;

/**
 * @Author: wenhongliang
 */
public class JwtUtil {
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    private static final String TOKEN_SECRET = "test-jwt-1013";

    public static String getToken(User user) {
        String token = JWT.create()
                .withAudience(user.getId()) // 需要保存在token的信息
                .sign(Algorithm.HMAC256(user.getPassword())); // 使用HS256生成token
        return token;
    }

    public static boolean verifyToken(String token, User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(user.getPassword());
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (IllegalArgumentException | JWTVerificationException e) {
            return false;
        }
    }

    public static String login(String username, String userId) {
        //过期时间
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        //私钥及加密算法
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        //设置头信息
        HashMap<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("algorithm", "HS256");
        //附带username和userID生成签名
        return JWT.create().withHeader(header)
                .withClaim("loginName", username)
                .withClaim("userId", userId)
                .withExpiresAt(date)
                .sign(algorithm);
    }

    public static boolean verity(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
