package com.cheersai.nexus.user;

import com.cheersai.nexus.user.config.BetaNotificationProperties;
import com.cheersai.nexus.user.config.BetaProvisionProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties({BetaProvisionProperties.class, BetaNotificationProperties.class})
@ComponentScan(basePackages = {
        "com.cheersai.nexus.user", 
        "com.cheersai.nexus.common"
})
@MapperScan("com.cheersai.nexus.user.mapper")
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }

}
