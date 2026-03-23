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
 * 会员计划审批/变更记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("plan_audit_logs")
public class PlanAuditLog {

    /**
     * 记录ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 关联的会员计划ID
     */
    @Column("plan_id")
    private String planId;

    /**
     * 操作类型：create-创建, update-更新, delete-删除, status-状态变更, benefit-权益配置
     */
    @Column("operate_type")
    private String operateType;

    /**
     * 审批状态：pending-待审批, approved-已通过, rejected-已驳回
     */
    @Column("audit_status")
    private String auditStatus;

    /**
     * 变更前数据（JSON格式）
     */
    @Column("before_data")
    private String beforeData;

    /**
     * 变更后数据（JSON格式）
     */
    @Column("after_data")
    private String afterData;

    /**
     * 审批人ID
     */
    @Column("auditor_id")
    private String auditorId;

    /**
     * 审批人名称
     */
    @Column("auditor_name")
    private String auditorName;

    /**
     * 审批备注
     */
    @Column("audit_remark")
    private String auditRemark;

    /**
     * 审批时间
     */
    @Column("audited_at")
    private LocalDateTime auditedAt;

    /**
     * 申请人ID
     */
    @Column("applicant_id")
    private String applicantId;

    /**
     * 申请人名称
     */
    @Column("applicant_name")
    private String applicantName;

    /**
     * 申请备注
     */
    @Column("apply_remark")
    private String applyRemark;

    /**
     * 申请时间
     */
    @Column("applied_at")
    private LocalDateTime appliedAt;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
