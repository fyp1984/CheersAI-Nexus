package com.cheersai.nexus.user;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserManagementApplicationTests {

    @Test
    void contextLoads() {
        String userCondition = "123";
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("user")
                .where(User::getUserId).like(userCondition)
                .or(User::getEmail).like(userCondition)
                .or(User::getPhone).like(userCondition)
                .or(User::getUsername).like(userCondition)
                .or(User::getNickname).like(userCondition)
                .or(User::getPasswordHash).like(userCondition)
                .or(User::getStatus).like(userCondition)
                .or(User::getEmailVerified).like(userCondition)
                .or(User::getPhoneVerified).like(userCondition)
                .or(User::getLastLoginAt).like(userCondition)
                .or(User::getLastLoginIp).like(userCondition)
                .or(User::getCreatedAt).like(userCondition)
                .or(User::getUpdatedAt).like(userCondition);

        System.out.println(wrapper.toSQL());
    }

}
