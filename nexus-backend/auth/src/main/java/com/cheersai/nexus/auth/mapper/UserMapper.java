package com.cheersai.nexus.auth.mapper;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT id, email, phone, username, nickname, avatar_url AS avatarUrl, "
            + "password_hash AS passwordHash, status, role, email_verified AS emailVerified, "
            + "phone_verified AS phoneVerified, last_login_at AS lastLoginAt, last_login_ip AS lastLoginIp, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM nexus.users WHERE LOWER(email) = LOWER(#{email}) LIMIT 1")
    User selectOneByEmailIgnoreCase(@Param("email") String email);

    @Select("SELECT id, email, phone, username, nickname, avatar_url AS avatarUrl, "
            + "password_hash AS passwordHash, status, role, email_verified AS emailVerified, "
            + "phone_verified AS phoneVerified, last_login_at AS lastLoginAt, last_login_ip AS lastLoginIp, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM nexus.users WHERE phone = #{phone} LIMIT 1")
    User selectOneByPhone(@Param("phone") String phone);

    @Select("SELECT id, email, phone, username, nickname, avatar_url AS avatarUrl, "
            + "password_hash AS passwordHash, status, role, email_verified AS emailVerified, "
            + "phone_verified AS phoneVerified, last_login_at AS lastLoginAt, last_login_ip AS lastLoginIp, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM nexus.users WHERE LOWER(username) = LOWER(#{username}) LIMIT 1")
    User selectOneByUsernameIgnoreCase(@Param("username") String username);

    @Select("SELECT id, email, phone, username, nickname, avatar_url AS avatarUrl, "
            + "password_hash AS passwordHash, status, role, email_verified AS emailVerified, "
            + "phone_verified AS phoneVerified, last_login_at AS lastLoginAt, last_login_ip AS lastLoginIp, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM nexus.users WHERE id = #{userId} LIMIT 1")
    User selectOneByUserId(@Param("userId") String userId);
}
