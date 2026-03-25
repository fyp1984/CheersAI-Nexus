package com.cheersai.nexus.auditlog.entity;

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
 * 审计日志实体类
 * 
 * 日志类型：
 * - user_action: 用户行为（登录、操作记录），保留90天
 * - admin_action: 管理操作（配置变更），保留365天
 * - system_event: 系统事件（启动、错误），保留30天
 * - security_event: 安全事件（登录失败、异常访问），保留365天
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_logs")
public class AuditLog {

    /**
     * 日志ID
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 日志类型：user_action/admin_action/system_event/security_event
     */
    @Column("log_type")
    private String logType;

    /**
     * 操作类型：如 login, logout, create_plan, update_subscription 等
     */
    private String action;

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
     * 目标类型：如 User, Subscription, MembershipPlan 等
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
    private String beforeData;

    /**
     * 操作后数据（JSON格式）
     */
    private String afterData;

    /**
     * IP地址
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @Column("user_agent")
    private String userAgent;

    /**
     * 操作结果：success/failure
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
    @Column("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
