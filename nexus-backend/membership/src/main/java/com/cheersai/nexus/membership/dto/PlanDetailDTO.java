package com.cheersai.nexus.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员计划详情DTO（返回用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailDTO {

    /**
     * 计划ID
     */
    private String id;

    /**
     * 计划编码
     */
    private String code;

    /**
     * 计划名称
     */
    private String name;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 月付价格
     */
    private BigDecimal priceMonthly;

    /**
     * 年付价格
     */
    private BigDecimal priceYearly;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 权益配置
     */
    private String features;

    /**
     * 额度配置
     */
    private String limits;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 当前审批状态
     */
    private String auditStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
