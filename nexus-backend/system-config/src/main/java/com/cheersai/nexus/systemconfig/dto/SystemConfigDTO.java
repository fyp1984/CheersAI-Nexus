package com.cheersai.nexus.systemconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统配置 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {

    /**
     * 注册策略配置
     */
    private RegisterConfig register;

    /**
     * 登录与安全配置
     */
    private SecurityConfig security;

    /**
     * Token与会话配置
     */
    private TokenConfig token;

    /**
     * IP白名单
     */
    private List<IpWhitelistDTO> ipWhitelist;

    /**
     * 注册策略
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterConfig {
        /**
         * 允许的注册方式
         */
        private List<String> registerMethods;

        /**
         * 是否强制邮箱验证
         */
        private Boolean forceEmailVerify;

        /**
         * 是否启用邀请码
         */
        private Boolean needInviteCode;

        /**
         * 默认会员方案
         */
        private String defaultMemberPlan;

        /**
         * 新用户自动激活
         */
        private Boolean autoActivate;
    }

    /**
     * 安全配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityConfig {
        /**
         * 登录方式
         */
        private String loginMode;

        /**
         * 登录验证码开关
         */
        private Boolean enableCaptcha;

        /**
         * 连续失败锁定阈值
         */
        private Integer failLockThreshold;

        /**
         * 锁定时长（分钟）
         */
        private Integer lockMinutes;

        /**
         * 启用双因子认证
         */
        private Boolean enable2FA;

        /**
         * 密码复杂度策略
         */
        private List<String> passwordPolicy;
    }

    /**
     * Token配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenConfig {
        /**
         * Access Token 有效期（小时）
         */
        private Integer accessTokenHours;

        /**
         * Refresh Token 有效期（天）
         */
        private Integer refreshTokenDays;

        /**
         * 允许并发会话数
         */
        private Integer maxSessionCount;

        /**
         * 异地登录提醒
         */
        private Boolean notifyLoginFromNewIp;

        /**
         * 空闲会话超时（分钟）
         */
        private Integer idleTimeoutMinutes;
    }

    /**
     * IP白名单项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IpWhitelistDTO {
        private String ip;
        private String remark;
        private String createTime;
    }
}