package com.zy.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 公共模块集成示例服务启动入口。
 */
@SpringBootApplication
public class DemoApplication {

    /**
     * 启动示例服务。
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
