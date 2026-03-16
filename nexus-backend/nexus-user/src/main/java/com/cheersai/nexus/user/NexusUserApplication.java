package com.cheersai.nexus.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cheersai.nexus.user.mapper")
public class NexusUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusUserApplication.class, args);
    }

}
