package com.cheersai.nexus.membership.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建会员计划DTO
 */
@Data
public class PlanCreateDTO {

    /**
     * 计划编码 - 唯一标识
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
     * 权益配置（JSON格式字符串）
     */
    private String features;

    /**
     * 额度配置（JSON格式字符串）
     */
    private String limits;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 初始状态
     */
    private String status;
}
