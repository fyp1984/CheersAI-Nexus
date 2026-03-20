package com.cheersai.nexus.feedback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户反馈模块启动类
 */
@SpringBootApplication(scanBasePackages = {"com.cheersai.nexus.common", "com.cheersai.nexus.feedback"})
@MapperScan("com.cheersai.nexus.feedback.mapper")
public class FeedbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedbackApplication.class, args);
    }

}
