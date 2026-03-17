package com.cheersai.nexus.model.usermanagement;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Table("user")
public class User {

    //UUID
    @Id
    private String userId;
    
    //邮箱
    private String email;
    
    //电话号码
    private String phone;

    //用户名
    private String username;

    //用户昵称
    private String nickname;

    //头像 URL
    @Column("avatar_url")
    private String avatarUrl;

    //用户密码哈希值
    @Column("password_hash")
    private String passwordHash;
    
    //状态（active/inactive/deleted）
    private String status;

    //邮箱是否验证
    @Column("email_verified")
    private Boolean emailVerified;

    //手机是否验证
    @Column("phone_verified")
    private Boolean phoneVerified;

    //最后登录时间
    @Column("last_login_at")
    private LocalDateTime lastLoginAt;

    //最后登录IP
    @Column("last_login_ip")
    private String lastLoginIp;

    //账号创建时间
    @Column("create_at")
    private LocalDateTime createdAt;

    //账号更新时间
    @Column("update_at")
    private LocalDateTime updatedAt;
}
