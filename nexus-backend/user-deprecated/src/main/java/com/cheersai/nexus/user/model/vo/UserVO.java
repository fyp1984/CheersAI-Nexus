package com.cheersai.nexus.user.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户详情VO
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 邮箱
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
    private String avatarUrl;

    /**
     * 状态
     */
    private String status;

    /**
     * 邮箱是否验证
     */
    private Boolean emailVerified;

    /**
     * 手机是否验证
     */
    private Boolean phoneVerified;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
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
