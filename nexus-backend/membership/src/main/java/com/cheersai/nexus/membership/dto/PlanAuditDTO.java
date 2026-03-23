package com.cheersai.nexus.membership.dto;

import lombok.Data;

/**
 * 会员计划审批操作DTO
 */
@Data
public class PlanAuditDTO {

    /**
     * 审批操作：approve-通过, reject-驳回
     */
    private String action;

    /**
     * 审批备注
     */
    private String auditRemark;
}
