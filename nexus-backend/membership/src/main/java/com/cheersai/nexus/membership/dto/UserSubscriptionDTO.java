package com.cheersai.nexus.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户会员信息DTO（返回用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户名
     */
    private String username;

    /**
     * 当前订阅ID
     */
    private String subscriptionId;

    /**
     * 当前计划编码
     */
    private String currentPlanCode;

    /**
     * 当前计划名称
     */
    private String currentPlanName;

    /**
     * 订阅状态
     */
    private String subscriptionStatus;

    /**
     * 到期日期
     */
    private LocalDate planExpire;

    /**
     * 是否自动续费
     */
    private Boolean autoRenew;
}
