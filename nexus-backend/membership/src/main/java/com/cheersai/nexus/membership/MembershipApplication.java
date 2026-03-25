package com.cheersai.nexus.membership;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 会员管理模块启动类
 */
@SpringBootApplication
@MapperScan("com.cheersai.nexus.membership.mapper")
@ComponentScan(basePackages = {
        "com.cheersai.nexus.membership",
        "com.cheersai.nexus.common"
})
public class MembershipApplication {

    public static void main(String[] args) {
        SpringApplication.run(MembershipApplication.class, args);
    }
}
