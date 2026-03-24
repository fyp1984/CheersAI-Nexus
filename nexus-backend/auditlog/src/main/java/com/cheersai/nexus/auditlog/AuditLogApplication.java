package com.cheersai.nexus.auditlog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.cheersai.nexus.auditlog",
        "com.cheersai.nexus.common"
})
@MapperScan("com.cheersai.nexus.auditlog.mapper")
public class AuditLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditLogApplication.class, args);
    }

}
