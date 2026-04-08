package com.cheersai.nexus.user.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户会话实体类
 * 对应表: nexus.user_sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.user_sessions")
public class UserSession {

    /**
     * 会话ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 关联用户ID
     */
    @Column("user_id")
    private String userId;

    /**
     * 会话ID（唯一）
     */
    @Column("session_id")
    private String sessionId;

    /**
     * 设备类型: web/ios/android/desktop
     */
    @Column("device_type")
    private String deviceType;

    /**
     * 设备名称
     */
    @Column("device_name")
    private String deviceName;

    /**
     * 登录IP
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * 浏览器/客户端信息
     */
    @Column("user_agent")
    private String userAgent;

    /**
     * 登录时间
     */
    @Builder.Default
    @Column("login_at")
    private LocalDateTime loginAt = LocalDateTime.now();

    /**
     * 最后活跃时间
     */
    @Builder.Default
    @Column("last_active_at")
    private LocalDateTime lastActiveAt = LocalDateTime.now();

    /**
     * 会话过期时间
     */
    @Column("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 是否已吊销
     */
    @Builder.Default
    @Column("revoked")
    private Boolean revoked = false;

    /**
     * 创建时间
     */
    @Builder.Default
    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
