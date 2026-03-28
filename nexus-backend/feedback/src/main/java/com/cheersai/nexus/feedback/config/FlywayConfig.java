package com.cheersai.nexus.feedback.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName:FlyAwayConfig
 * @Description:TODO
 * @Author:userSigma
 * @CreateDate:2026/3/28 08:45
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.repair();  // 先修复校验和
            flyway.migrate(); // 再执行迁移
        };
    }
}
