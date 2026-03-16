package com.cheersai.nexus.user.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Table("sys_user")
public class User {

    /**
     * 用户唯一标识
     */
    @Id
    private String userId;

    /**
     * 邮箱（唯一）
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    @Column("avatar_url")
    private String avatarUrl;

    /**
     * 密码哈希
     */
    @Column("password_hash")
    private String passwordHash;

    /**
     * 状态（active/inactive/deleted）
     */
    private String status;

    /**
     * 邮箱是否验证
     */
    @Column("email_verified")
    private Boolean emailVerified;

    /**
     * 手机是否验证
     */
    @Column("phone_verified")
    private Boolean phoneVerified;

    /**
     * 最后登录时间
     */
    @Column("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @Column("last_login_ip")
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
