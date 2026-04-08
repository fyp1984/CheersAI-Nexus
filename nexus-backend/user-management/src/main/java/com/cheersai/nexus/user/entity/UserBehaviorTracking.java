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
 * 用户行为追踪实体类
 * 对应表: nexus.user_behavior_tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.user_behavior_tracking")
public class UserBehaviorTracking {

    /**
     * 记录ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 关联用户ID（可为空，用于未登录用户）
     */
    @Column("user_id")
    private String userId;

    /**
     * 事件类型
     */
    @Column("event_type")
    private String eventType;

    /**
     * 事件数据（JSON格式）
     */
    @Column("event_data")
    private String eventData;

    /**
     * IP地址
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * 地理位置
     */
    @Column("location")
    private String location;

    /**
     * 设备指纹
     */
    @Column("device_fingerprint")
    private String deviceFingerprint;

    /**
     * 会话ID
     */
    @Column("session_id")
    private String sessionId;

    /**
     * 创建时间
     */
    @Builder.Default
    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
