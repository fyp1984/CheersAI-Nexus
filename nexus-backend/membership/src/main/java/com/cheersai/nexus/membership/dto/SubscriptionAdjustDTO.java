package com.cheersai.nexus.membership.dto;

import lombok.Data;

/**
 * 手动调整用户会员DTO
 */
@Data
public class SubscriptionAdjustDTO {

    /**
     * 目标会员计划编码
     */
    private String planCode;

    /**
     * 调整后到期天数（从今天开始计算，0表示永久有效）
     */
    private Integer expireDays;

    /**
     * 调整原因（必填，用于审计）
     */
    private String reason;
}
