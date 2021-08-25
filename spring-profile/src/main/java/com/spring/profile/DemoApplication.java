package com.spring.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        ConfigurableEnvironment environment = context.getEnvironment();
        environment.getPropertySources();
        Map<String, Object> map = new HashMap<>();
        map.put("local.ip", "111.111.111.111");
        environment.getPropertySources().addAfter("systemProperties", new MapPropertySource("DemoPropertySource", map));
        System.out.println(">>>>>>>>>>>>>>>>>>>>");

        UserConfig userConfig = context.getBean(UserConfig.class);
        userConfig.show();
        System.out.println(">>>>>>>>>>>>>>>>>>>>");

        environment.getPropertySources();
        context.close();
    }
}
