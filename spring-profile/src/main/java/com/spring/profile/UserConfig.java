package com.spring.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Author: wenhongliang
 */
@Component
public class UserConfig {
    @Autowired
    private Environment environment;

    @Value("${local.name}")
    private String localName;

    @Value("${local.port:9090}")
    private int port;

    public void show() {
        System.out.println("--------------------");
        System.out.println("localName: " + localName);
        System.out.println("port: " + port);
        System.out.println("--------------------");
        System.out.println("local.ip: " + environment.getProperty("local.ip"));
        //重载方法，使得读取到的数据是Integer类型的
        System.out.println("local.port: " + environment.getProperty("local.port", Integer.class));
        System.out.println("--------------------");
        //在配置文件中引用引用已有的变量
        System.out.println("local.url: " + environment.getProperty("local.url"));
        System.out.println("--------------------");
    }
}
