package com.cheersai.nexus.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订阅详情DTO（返回用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDetailDTO {

    /**
     * 订阅ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会员计划编码
     */
    private String planCode;

    /**
     * 会员计划名称
     */
    private String planName;

    /**
     * 订阅状态
     */
    private String status;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 是否自动续费
     */
    private Boolean autoRenew;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 最后支付时间
     */
    private LocalDateTime lastPaymentAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
