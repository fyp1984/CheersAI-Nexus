package com.cheersai.nexus.membership.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 创建订阅DTO
 */
@Data
public class SubscriptionCreateDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会员计划编码
     */
    private String planCode;

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
    
    private String status;
}
