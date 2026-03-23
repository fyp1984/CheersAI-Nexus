package com.cheersai.nexus.membership.entity;

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
 * 用户订阅变更审计日志实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("subscription_audit_logs")
public class SubscriptionAuditLog {

    /**
     * 记录ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 订阅ID
     */
    @Column("subscription_id")
    private String subscriptionId;

    /**
     * 用户ID
     */
    @Column("user_id")
    private String userId;

    /**
     * 操作类型：adjust-手动调整, upgrade-升级, downgrade-降级, extend-延长, shorten-缩短
     */
    @Column("operate_type")
    private String operateType;

    /**
     * 变更前计划编码
     */
    @Column("before_plan_code")
    private String beforePlanCode;

    /**
     * 变更后计划编码
     */
    @Column("after_plan_code")
    private String afterPlanCode;

    /**
     * 变更前到期日期
     */
    @Column("before_end_date")
    private String beforeEndDate;

    /**
     * 变更后到期日期
     */
    @Column("after_end_date")
    private String afterEndDate;

    /**
     * 操作原因/备注
     */
    @Column("reason")
    private String reason;

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
     * 操作人IP
     */
    @Column("operator_ip")
    private String operatorIp;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
