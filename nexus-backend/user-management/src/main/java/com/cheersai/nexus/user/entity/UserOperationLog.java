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
 * 用户操作日志实体类
 * 对应表: nexus.user_operation_logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.user_operation_logs")
public class UserOperationLog {

    /**
     * 日志ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 关联用户ID
     */
    @Column("user_id")
    private String userId;

    /**
     * 操作类型: create/update/delete/status/role/member
     */
    private String action;

    /**
     * 目标类型: user/profile/subscription
     */
    @Column("target_type")
    private String targetType;

    /**
     * 目标ID
     */
    @Column("target_id")
    private String targetId;

    /**
     * 操作前数据（JSON格式）
     */
    @Column("before_data")
    private String beforeData;

    /**
     * 操作后数据（JSON格式）
     */
    @Column("after_data")
    private String afterData;

    /**
     * 操作人ID
     */
    @Column("operator_id")
    private String operatorId;

    /**
     * 操作人名称
     */
    @Column("operator_name")
    private String operatorName;

    /**
     * IP地址
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * 浏览器/客户端信息
     */
    @Column("user_agent")
    private String userAgent;

    /**
     * 操作结果: success/failure
     */
    @Builder.Default
    private String result = "success";

    /**
     * 错误信息
     */
    @Column("error_message")
    private String errorMessage;

    /**
     * 创建时间
     */
    @Builder.Default
    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
