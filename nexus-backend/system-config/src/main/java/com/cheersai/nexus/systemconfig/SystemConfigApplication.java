package com.cheersai.nexus.systemconfig;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan
@ComponentScan(basePackages = {
        "com.cheersai.nexus.systemconfig",
        "com.cheersai.nexus.common"
})
public class SystemConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemConfigApplication.class, args);
    }
}