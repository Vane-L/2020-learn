package com.sofa.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: wenhongliang
 */
@SpringBootApplication
public class SofaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SofaApplication.class, args);
    }

    /**
     * 查看使用 Maven 插件生成的版本信息
     * http://localhost:8080/actuator/versions
     * 查看应用 Readiness Check 的状况
     * http://localhost:8080/actuator/readiness
     */
}
